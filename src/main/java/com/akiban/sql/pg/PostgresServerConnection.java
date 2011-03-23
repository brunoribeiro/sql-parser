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
  private static final Logger LOG = LoggerFactory.getLogger(PostgresServerConnection.class);

  static class DummyPreparedStatement {
    String m_sql;
    int[] m_paramTypes;
    DummyPreparedStatement(String sql, int[] paramTypes) {
      m_sql = sql;
      m_paramTypes = paramTypes;
    }
  }
  static class DummyStatementBinding {
    DummyPreparedStatement m_statement;
    Object[] m_parameters;
    boolean[] m_resultsBinary;
    DummyStatementBinding(DummyPreparedStatement statement, Object[] parameters, 
                          boolean[] resultsBinary) {
      m_statement = statement;
      m_parameters = parameters;
      m_resultsBinary = resultsBinary;
    }
  }

  private PostgresServer m_server;
  private boolean m_running = false;
  private Socket m_socket;
  private PostgresMessenger m_messenger;
  private int m_pid, m_secret;
  private int m_version;
  private Properties m_properties;
  private Map<String,DummyPreparedStatement> m_preparedStatements =
    new HashMap<String,DummyPreparedStatement>();
  private Map<String,DummyStatementBinding> m_boundPortals =
    new HashMap<String,DummyStatementBinding>();

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
      LOG.warn("Error in server", ex);
    }
    finally {
      try {
        m_socket.close();
      }
      catch (IOException ex) {
      }
    }
  }

  protected void topLevel() throws Exception {
    LOG.warn("Connect from {}" + m_socket.getRemoteSocketAddress());
    m_messenger.readMessage(false);
    processStartupMessage();
    while (m_running) {
      int type = m_messenger.readMessage();
      switch (type) {
      case -1:                  // EOF
        stop();
        break;
      case PostgresMessenger.PASSWORD_MESSAGE_TYPE:
        processPasswordMessage();
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
      case PostgresMessenger.TERMINATE_TYPE:
        processTerminate();
        break;
      default:
        throw new Exception("Unknown message type: " + (char)type);
      }
    }
    m_server.removeConnection(m_pid);
  }

  protected void processStartupMessage() throws Exception {
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
      LOG.warn("Version {}.{}", (version >> 16), (version & 0xFFFF));
    }
    m_properties = new Properties();
    while (true) {
      String param = m_messenger.readString();
      if (param.length() == 0) break;
      String value = m_messenger.readString();
      m_properties.put(param, value);
    }
    LOG.warn("Properties: {}", m_properties);
    String enc = m_properties.getProperty("client_encoding");
    if (enc != null) {
      if ("UNICODE".equals(enc))
        m_messenger.setEncoding("UTF-8");
      else
        m_messenger.setEncoding(enc);
    }
    {
      m_messenger.beginMessage(PostgresMessenger.AUTHENTICATION_TYPE);
      m_messenger.writeInt(PostgresMessenger.AUTHENTICATION_CLEAR_TEXT);
      m_messenger.sendMessage(true);
    }
  }

  protected void processCancelRequest() throws Exception {
    int pid = m_messenger.readInt();
    int secret = m_messenger.readInt();
    PostgresServerConnection connection = m_server.getConnection(pid);
    if ((connection != null) && (secret == connection.m_secret))
      // No easy way to signal in another thread.
      connection.m_messenger.setCancel(true);
    stop();                     // That's all for this connection.
  }

  protected void processSSLMessage() throws Exception {
    throw new Exception("NIY");
  }

  protected void processPasswordMessage() throws Exception {
    String user = m_properties.getProperty("user");
    String pass = m_messenger.readString();
    LOG.warn("Login {}/{}", user, pass);
    Properties status = new Properties();
    // This is enough to make the JDBC driver happy.
    status.put("client_encoding", m_properties.getProperty("client_encoding"));
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

  protected void processParse() throws Exception {
    String stmtName = m_messenger.readString();
    String query = m_messenger.readString();
    short nparams = m_messenger.readShort();
    int[] paramTypes = new int[nparams];
    for (int i = 0; i < nparams; i++)
      paramTypes[i] = m_messenger.readInt();
    m_preparedStatements.put(stmtName, new DummyPreparedStatement(query, paramTypes));
    LOG.warn("Query: {}", query);
    m_messenger.beginMessage(PostgresMessenger.PARSE_COMPLETE_TYPE);
    m_messenger.sendMessage();
  }

  protected void processBind() throws Exception {
    String portalName = m_messenger.readString();
    String stmtName = m_messenger.readString();
    short nformats = m_messenger.readShort();
    boolean[] paramsBinary = new boolean[nformats];
    for (int i = 0; i < nformats; i++)
      paramsBinary[i] = (m_messenger.readShort() == 1);
    short nparams = m_messenger.readShort();
    Object[] params = new Object[nparams];
    boolean binary = false;
    for (int i = 0; i < nparams; i++) {
      if (i < nformats)
        binary = paramsBinary[i];
      int len = m_messenger.readInt();
      if (len < 0) continue;    // Null
      byte[] param = new byte[len];
      m_messenger.readFully(param, 0, len);
      if (binary) {
        params[i] = param;
      }
      else {
        params[i] = new String(param, m_messenger.getEncoding());
      }
    }
    short nresults = m_messenger.readShort();
    boolean[] resultsBinary = new boolean[nresults];
    for (int i = 0; i < nresults; i++) {
      resultsBinary[i] = (m_messenger.readShort() == 1);
    }
    DummyPreparedStatement stmt = m_preparedStatements.get(stmtName);
    m_boundPortals.put(portalName, 
                       new DummyStatementBinding(stmt, params, resultsBinary));
    m_messenger.beginMessage(PostgresMessenger.BIND_COMPLETE_TYPE);
    m_messenger.sendMessage();
  }

  protected void processDescribe() throws Exception {
    byte source = m_messenger.readByte();
    String name = m_messenger.readString();
    DummyPreparedStatement stmt;
    boolean[] resultsBinary = null;
    switch (source) {
    case (byte)'S':
      stmt = m_preparedStatements.get(name);
      break;
    case (byte)'P':
      stmt = m_boundPortals.get(name).m_statement;
      break;
    default:
      throw new Exception("Unknown describe source: " + (char)source);
    }
    m_messenger.beginMessage(PostgresMessenger.ROW_DESCRIPTION_TYPE);
    int nfields = 2;
    String[] names = new String[] { "foo", "bar" };
    m_messenger.writeShort(nfields);
    boolean binary = false;
    for (int i = 0; i < nfields; i++) {
      if ((resultsBinary != null) && (i < resultsBinary.length))
        binary = resultsBinary[i];
      m_messenger.writeString(names[i]); // attname
      m_messenger.writeInt(0);              // attrelid
      m_messenger.writeShort(0);            // attnum
      m_messenger.writeInt(PostgresType.VARCHAR_TYPE_OID); // atttypid
      m_messenger.writeShort(-1);           // attlen
      m_messenger.writeInt(0);              // atttypmod
      m_messenger.writeShort(binary ? 0 : 1);
    }
    m_messenger.sendMessage();
  }

  protected void processExecute() throws Exception {
    String portalName = m_messenger.readString();
    int maxrows = m_messenger.readInt();
    DummyStatementBinding binding = m_boundPortals.get(portalName);
    int nrows = 3;
    if (nrows == 0) {
      m_messenger.beginMessage(PostgresMessenger.NO_DATA_TYPE);
      m_messenger.sendMessage();
    }
    else {
      if ((maxrows > 0) && (maxrows < nrows))
        nrows = maxrows;
      short ncols = 2;
      for (int j = 0; j < nrows; j++) {
        m_messenger.beginMessage(PostgresMessenger.DATA_ROW_TYPE);
        m_messenger.writeShort(ncols);
        boolean binary = false;
        for (int i = 0; i < ncols; i++) {
          if (i < binding.m_resultsBinary.length)
            binary = binding.m_resultsBinary[i];
          String value = (i < 1) ? Integer.toString(j) : null;
          int len = (value == null) ? -1 : value.length();
          byte[] bv = null;
          if ((len > 0) && !binary)
            bv = value.getBytes(m_messenger.getEncoding());
          m_messenger.writeInt(len);
          if (bv != null)
            m_messenger.write(bv);
        }
        m_messenger.sendMessage();
      }
    }
    m_messenger.beginMessage(PostgresMessenger.COMMAND_COMPLETE_TYPE);
    m_messenger.writeString("SELECT");
    m_messenger.sendMessage();
  }

  protected void processSync() throws Exception {
    m_messenger.beginMessage(PostgresMessenger.READY_FOR_QUERY_TYPE);
    m_messenger.writeByte('I'); // Idle ('T' -> xact open; 'E' -> xact abort)
    m_messenger.sendMessage(true);
  }

  protected void processTerminate() throws Exception {
    stop();
  }

}
