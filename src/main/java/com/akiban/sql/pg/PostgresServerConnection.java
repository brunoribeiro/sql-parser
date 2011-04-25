/* Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.akiban.sql.pg;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.CursorNode;

import com.akiban.ais.model.AkibanInformationSchema;
import com.akiban.ais.model.Column;
import com.akiban.ais.model.UserTable;
import com.akiban.server.service.ServiceManager;
import com.akiban.server.service.ServiceManagerImpl;
import com.akiban.server.service.session.Session;
import com.akiban.server.service.session.SessionImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Connection to a Postgres server client.
 * Runs in its own thread; has its own AkServer Session.
 *
 */
public class PostgresServerConnection implements Runnable
{
  private static final Logger g_logger = LoggerFactory.getLogger(PostgresServerConnection.class);

  private PostgresServer m_server;
  private boolean m_running = false, m_ignoreUntilSync = false;
  private Socket m_socket;
  private PostgresMessenger m_messenger;
  private int m_pid, m_secret;
  private int m_version;
  private Properties m_properties;
  private Map<String,PostgresStatement> m_preparedStatements =
    new HashMap<String,PostgresStatement>();
  private Map<String,PostgresStatement> m_boundPortals =
    new HashMap<String,PostgresStatement>();

  private Session m_session;
  private ServiceManager m_serviceManager;
  private AkibanInformationSchema m_ais;
  private SQLParser m_parser;
  private PostgresStatementCompiler m_compiler;

  public PostgresServerConnection(PostgresServer server, Socket socket, 
                                  int pid, int secret) {
    m_server = server;
    m_socket = socket;
    m_pid = pid;
    m_secret = secret;
  }

  public void start() {
    m_running = true;
    new Thread(this).start();
  }

  public void stop() {
    m_running = false;
    // Can only wake up stream read by closing down socket.
    try {
      m_socket.close();
    }
    catch (IOException ex) {
    }
  }

  public void run() {
    try {
      m_messenger = new PostgresMessenger(m_socket.getInputStream(),
                                          m_socket.getOutputStream());
      topLevel();
    }
    catch (Exception ex) {
      if (m_running)
        g_logger.warn("Error in server", ex);
    }
    finally {
      try {
        m_socket.close();
      }
      catch (IOException ex) {
      }
    }
  }

  protected void topLevel() throws IOException {
    g_logger.warn("Connect from {}" + m_socket.getRemoteSocketAddress());
    m_messenger.readMessage(false);
    processStartupMessage();
    while (m_running) {
      int type = m_messenger.readMessage();
      if (m_ignoreUntilSync) {
        if ((type != -1) && (type != PostgresMessenger.SYNC_TYPE))
          continue;
        m_ignoreUntilSync = false;
      }
      try {
        switch (type) {
        case -1:                  // EOF
          stop();
          break;
        case PostgresMessenger.PASSWORD_MESSAGE_TYPE:
          processPasswordMessage();
          break;
        case PostgresMessenger.QUERY_TYPE:
          processQuery();
          break;
        case PostgresMessenger.PARSE_TYPE:
          processParse();
          break;
        case PostgresMessenger.BIND_TYPE:
          processBind();
          break;
        case PostgresMessenger.DESCRIBE_TYPE:
          processDescribe();
          break;
        case PostgresMessenger.EXECUTE_TYPE:
          processExecute();
          break;
        case PostgresMessenger.SYNC_TYPE:
          processSync();
          break;
        case PostgresMessenger.CLOSE_TYPE:
          processClose();
          break;
        case PostgresMessenger.TERMINATE_TYPE:
          processTerminate();
          break;
        default:
          throw new IOException("Unknown message type: " + (char)type);
        }
      }
      catch (StandardException ex) {
        g_logger.warn("Error in query", ex);
        m_messenger.beginMessage(PostgresMessenger.ERROR_RESPONSE_TYPE);
        m_messenger.write('S');
        m_messenger.writeString("ERROR");
        // TODO: Could dummy up an SQLSTATE, etc.
        m_messenger.write('M');
        m_messenger.writeString(ex.getMessage());
        m_messenger.write(0);
        m_messenger.sendMessage(true);
        m_ignoreUntilSync = true;
      }
    }
    m_server.removeConnection(m_pid);
  }

  protected void processStartupMessage() throws IOException {
    int version = m_messenger.readInt();
    switch (version) {
    case PostgresMessenger.VERSION_CANCEL:
      processCancelRequest();
      return;
    case PostgresMessenger.VERSION_SSL:
      processSSLMessage();
      return;
    default:
      m_version = version;
      g_logger.warn("Version {}.{}", (version >> 16), (version & 0xFFFF));
    }
    m_properties = new Properties();
    while (true) {
      String param = m_messenger.readString();
      if (param.length() == 0) break;
      String value = m_messenger.readString();
      m_properties.put(param, value);
    }
    g_logger.warn("Properties: {}", m_properties);
    String enc = m_properties.getProperty("client_encoding");
    if (enc != null) {
      if ("UNICODE".equals(enc))
        m_messenger.setEncoding("UTF-8");
      else
        m_messenger.setEncoding(enc);
    }
    
    String schema = m_properties.getProperty("database");
    m_session = new SessionImpl();
    m_serviceManager = ServiceManagerImpl.get();
    m_ais = m_serviceManager.getDStarL().ddlFunctions().getAIS(m_session);
    m_parser = new SQLParser();
    if (false)
      m_compiler = new PostgresHapiCompiler(m_parser, m_ais, schema);
    else
      m_compiler = new PostgresOperatorCompiler(m_parser, m_ais, schema,
                                                m_session, m_serviceManager);

    {
      m_messenger.beginMessage(PostgresMessenger.AUTHENTICATION_TYPE);
      m_messenger.writeInt(PostgresMessenger.AUTHENTICATION_CLEAR_TEXT);
      m_messenger.sendMessage(true);
    }
  }

  protected void processCancelRequest() throws IOException {
    int pid = m_messenger.readInt();
    int secret = m_messenger.readInt();
    PostgresServerConnection connection = m_server.getConnection(pid);
    if ((connection != null) && (secret == connection.m_secret))
      // No easy way to signal in another thread.
      connection.m_messenger.setCancel(true);
    stop();                     // That's all for this connection.
  }

  protected void processSSLMessage() throws IOException {
    throw new IOException("NIY");
  }

  protected void processPasswordMessage() throws IOException {
    String user = m_properties.getProperty("user");
    String pass = m_messenger.readString();
    g_logger.warn("Login {}/{}", user, pass);
    Properties status = new Properties();
    // This is enough to make the JDBC driver happy.
    status.put("client_encoding", m_properties.getProperty("client_encoding", "UNICODE"));
    status.put("server_encoding", m_messenger.getEncoding());
    status.put("server_version", "8.4.7"); // Not sure what the min it'll accept is.
    status.put("session_authorization", user);
    
    {
      m_messenger.beginMessage(PostgresMessenger.AUTHENTICATION_TYPE);
      m_messenger.writeInt(PostgresMessenger.AUTHENTICATION_OK);
      m_messenger.sendMessage();
    }
    for (String prop : status.stringPropertyNames()) {
      m_messenger.beginMessage(PostgresMessenger.PARAMETER_STATUS_TYPE);
      m_messenger.writeString(prop);
      m_messenger.writeString(status.getProperty(prop));
      m_messenger.sendMessage();
    }
    {
      m_messenger.beginMessage(PostgresMessenger.BACKEND_KEY_DATA_TYPE);
      m_messenger.writeInt(m_pid);
      m_messenger.writeInt(m_secret);
      m_messenger.sendMessage();
    }
    {
      m_messenger.beginMessage(PostgresMessenger.READY_FOR_QUERY_TYPE);
      m_messenger.writeByte('I'); // Idle ('T' -> xact open; 'E' -> xact abort)
      m_messenger.sendMessage(true);
    }
  }

  // ODBC driver sends this at the start; returning no rows is fine (and normal).
  public static final String ODBC_LO_TYPE_QUERY = "select oid, typbasetype from pg_type where typname = 'lo'";

  protected void processQuery() throws IOException, StandardException {
    String sql = m_messenger.readString();
    g_logger.warn("Query: {}", sql);
    if (!sql.equals(ODBC_LO_TYPE_QUERY)) {
      StatementNode stmt = m_parser.parseStatement(sql);
      if (!(stmt instanceof CursorNode))
        throw new StandardException("Not a SELECT");
      PostgresStatement pstmt = m_compiler.compile((CursorNode)stmt, null);
      pstmt.sendRowDescription(m_messenger);
      int nrows = pstmt.execute(m_messenger, m_session, -1);
    }
    m_messenger.beginMessage(PostgresMessenger.COMMAND_COMPLETE_TYPE);
    m_messenger.writeString("SELECT");
    m_messenger.sendMessage();
    m_messenger.beginMessage(PostgresMessenger.READY_FOR_QUERY_TYPE);
    m_messenger.writeByte('I');
    m_messenger.sendMessage(true);
  }

  protected void processParse() throws IOException, StandardException {
    String stmtName = m_messenger.readString();
    String sql = m_messenger.readString();
    // TODO: $n might be out of order.
    sql = sql.replaceAll("\\$.", "?");
    short nparams = m_messenger.readShort();
    int[] paramTypes = new int[nparams];
    for (int i = 0; i < nparams; i++)
      paramTypes[i] = m_messenger.readInt();
    g_logger.warn("Parse: {}", sql);

    StatementNode stmt = m_parser.parseStatement(sql);
    if (stmt instanceof CursorNode) {
      PostgresStatement pstmt = m_compiler.compile((CursorNode)stmt, paramTypes);
      m_preparedStatements.put(stmtName, pstmt);
    }
    else
      throw new StandardException("Not a SELECT");

    m_messenger.beginMessage(PostgresMessenger.PARSE_COMPLETE_TYPE);
    m_messenger.sendMessage();
  }

  protected void processBind() throws IOException {
    String portalName = m_messenger.readString();
    String stmtName = m_messenger.readString();
    String[] params = null;
    {
      short nformats = m_messenger.readShort();
      boolean[] paramsBinary = new boolean[nformats];
      for (int i = 0; i < nformats; i++)
        paramsBinary[i] = (m_messenger.readShort() == 1);
      short nparams = m_messenger.readShort();
      params = new String[nparams];
      boolean binary = false;
      for (int i = 0; i < nparams; i++) {
        if (i < nformats)
          binary = paramsBinary[i];
        int len = m_messenger.readInt();
        if (len < 0) continue;    // Null
        byte[] param = new byte[len];
        m_messenger.readFully(param, 0, len);
        if (binary) {
          throw new IOException("Don't know how to parse binary format.");
        }
        else {
          params[i] = new String(param, m_messenger.getEncoding());
        }
      }
    }
    boolean[] resultsBinary = null; 
    boolean defaultResultsBinary = false;
    {    
      short nresults = m_messenger.readShort();
      if (nresults == 1)
        defaultResultsBinary = (m_messenger.readShort() == 1);
      else if (nresults > 0) {
        resultsBinary = new boolean[nresults];
        for (int i = 0; i < nresults; i++) {
          resultsBinary[i] = (m_messenger.readShort() == 1);
        }
        defaultResultsBinary = resultsBinary[nresults-1];
      }
    }
    PostgresStatement pstmt = m_preparedStatements.get(stmtName);
    m_boundPortals.put(portalName, 
                       pstmt.getBoundRequest(params, 
                                             resultsBinary, defaultResultsBinary));
    m_messenger.beginMessage(PostgresMessenger.BIND_COMPLETE_TYPE);
    m_messenger.sendMessage();
  }

  protected void processDescribe() throws IOException, StandardException {
    byte source = m_messenger.readByte();
    String name = m_messenger.readString();
    PostgresStatement pstmt;    
    switch (source) {
    case (byte)'S':
      pstmt = m_preparedStatements.get(name);
      break;
    case (byte)'P':
      pstmt = m_boundPortals.get(name);
      break;
    default:
      throw new IOException("Unknown describe source: " + (char)source);
    }
    if (false) {
      // This would be for a query not returning data.
      m_messenger.beginMessage(PostgresMessenger.NO_DATA_TYPE);
      m_messenger.sendMessage();
    }
    else {
      pstmt.sendRowDescription(m_messenger);
    }
  }

  protected void processExecute() throws IOException, StandardException {
    String portalName = m_messenger.readString();
    int maxrows = m_messenger.readInt();
    PostgresStatement pstmt = m_boundPortals.get(portalName);
    int nrows = pstmt.execute(m_messenger, m_session, maxrows);
    m_messenger.beginMessage(PostgresMessenger.COMMAND_COMPLETE_TYPE);
    m_messenger.writeString("SELECT");
    m_messenger.sendMessage();
  }

  protected void processSync() throws IOException {
    m_messenger.beginMessage(PostgresMessenger.READY_FOR_QUERY_TYPE);
    m_messenger.writeByte('I'); // Idle ('T' -> xact open; 'E' -> xact abort)
    m_messenger.sendMessage(true);
  }

  protected void processClose() throws IOException {
    byte source = m_messenger.readByte();
    String name = m_messenger.readString();
    PostgresStatement pstmt;    
    switch (source) {
    case (byte)'S':
      pstmt = m_preparedStatements.remove(name);
      break;
    case (byte)'P':
      pstmt = m_boundPortals.remove(name);
      break;
    default:
      throw new IOException("Unknown describe source: " + (char)source);
    }
    m_messenger.beginMessage(PostgresMessenger.CLOSE_COMPLETE_TYPE);
    m_messenger.sendMessage();
  }
  
  protected void processTerminate() throws IOException {
    stop();
  }

}
