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

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Basic implementation of Postgres wire protocol for SQL integration.
 *
 * See http://developer.postgresql.org/pgdocs/postgres/protocol.html
 */
public class PostgresServer implements Runnable, DataInput, DataOutput
{
  public static final int DEFAULT_PORT = 15432; // Real one is 5432

  /*** Message Formats ***/
  public static final int AUTHENTICATION_TYPE = 'R'; // (B)
  public static final int BACKEND_KEY_DATA_TYPE = 'K'; // (B)
  public static final int BIND_TYPE = 'B'; // (F)
  public static final int BIND_COMPLETE_TYPE = '2'; // (B)
  public static final int CLOSE_TYPE = 'C'; // (F)
  public static final int CLOSE_COMPLETE_TYPE = '3'; // (B)
  public static final int COMMAND_COMPLETE_TYPE = 'C'; // (B)
  public static final int COPY_DATA_TYPE = 'd'; // (F & B)
  public static final int COPY_DONE_TYPE = 'c'; // (F & B)
  public static final int COPY_FAIL_TYPE = 'f'; // (F)
  public static final int COPY_IN_RESPONSE_TYPE = 'G'; // (B)
  public static final int COPY_OUT_RESPONSE_TYPE = 'H'; // (B)
  public static final int COPY_BOTH_RESPONSE_TYPE = 'W'; // (B)
  public static final int DATA_ROW_TYPE = 'D'; // (B)
  public static final int DESCRIBE_TYPE = 'D'; // (F)
  public static final int EMPTY_QUERY_RESPONSE_TYPE = 'I'; // (B)
  public static final int ERROR_RESPONSE_TYPE = 'E'; // (B)
  public static final int EXECUTE_TYPE = 'E'; // (F)
  public static final int FLUSH_TYPE = 'H'; // (F)
  public static final int FUNCTION_CALL_TYPE = 'F'; // (F)
  public static final int FUNCTION_CALL_RESPONSE_TYPE = 'V'; // (B)
  public static final int NO_DATA_TYPE = 'n'; // (B)
  public static final int NOTICE_RESPONSE_TYPE = 'N'; // (B)
  public static final int NOTIFICATION_RESPONSE_TYPE = 'A'; // (B)
  public static final int PARAMETER_DESCRIPTION_TYPE = 't'; // (B)
  public static final int PARAMETER_STATUS_TYPE = 'S'; // (B)
  public static final int PARSE_TYPE = 'P'; // (F)
  public static final int PARSE_COMPLETE_TYPE = '1'; // (B)
  public static final int PASSWORD_MESSAGE_TYPE = 'p'; // (F)
  public static final int PORTAL_SUSPENDED_TYPE = 's'; // (B)
  public static final int QUERY_TYPE = 'Q'; // (F)
  public static final int READY_FOR_QUERY_TYPE = 'Z'; // (B)
  public static final int ROW_DESCRIPTION_TYPE = 'T'; // (B)
  public static final int STARTUP_MESSAGE_TYPE = 0; // (F)
  public static final int SYNC_TYPE = 'S'; // (F)
  public static final int TERMINATE_TYPE = 'X'; // (F)
  
  public static final int VERSION_CANCEL = 80877102; // 12345678
  public static final int VERSION_SSL = 80877103; // 12345679
  
  public static final int AUTHENTICATION_OK = 0;
  public static final int AUTHENTICATION_KERBEROS_V5 = 2;
  public static final int AUTHENTICATION_CLEAR_TEXT = 3;
  public static final int AUTHENTICATION_MD5 = 5;
  public static final int AUTHENTICATION_SCM = 6;
  public static final int AUTHENTICATION_GSS = 7;
  public static final int AUTHENTICATION_SSPI = 9;
  public static final int AUTHENTICATION_GSS_CONTINUE = 8;

  public static final int VARCHAR_TYPE_OID = 1043;

  public static void main(String[] args) throws Exception {
    int port = DEFAULT_PORT;
    int i = 0;
    while (i < args.length) {
      String arg = args[i++];
      if ("-port".equals(arg)) {
        port = Integer.parseInt(args[i++]);
      }
      else {
        throw new Exception("Unknown argument: " + arg);
      }
    }
    serverTopLevel(port);
  }
  
  protected static Map<Integer,PostgresServer> g_servers = 
    new HashMap<Integer,PostgresServer>();

  protected static void serverTopLevel(int port) throws Exception {
    ServerSocket server = new ServerSocket(port);
    try {
      System.out.println("Listening on port " + port);
      int pid = 0;
      Random rand = new Random();
      while (true) {
        Socket socket = server.accept();
        pid++;
        int secret = rand.nextInt();
        PostgresServer pg = new PostgresServer(socket, pid, secret);
        g_servers.put(pid, pg);
        new Thread(pg).start();
      }
    }
    finally {
      server.close();
    }
  }

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

  private Socket m_socket;
  private InputStream m_inputStream;
  private OutputStream m_outputStream;
  private DataInputStream m_dataInput;
  private DataInputStream m_messageInput;
  private ByteArrayOutputStream m_byteOutput;
  private DataOutputStream m_messageOutput;
  private String m_encoding = "ISO-8859-1";
  private boolean m_running = false, m_cancel = false;
  private int m_pid, m_secret;
  private int m_version;
  private Properties m_properties;
  private Map<String,DummyPreparedStatement> m_preparedStatements =
    new HashMap<String,DummyPreparedStatement>();
  private Map<String,DummyStatementBinding> m_boundPortals =
    new HashMap<String,DummyStatementBinding>();

  public PostgresServer(Socket socket, int pid, int secret) {
    m_socket = socket;
    m_pid = pid;
    m_secret = secret;
  }

  public void run() {
    try {
      m_inputStream = m_socket.getInputStream();
      m_outputStream = m_socket.getOutputStream();
      m_dataInput = new DataInputStream(m_inputStream);
      topLevel();
    }
    catch (Exception ex) {
      ex.printStackTrace(System.err);
    }
    finally {
      if (m_outputStream != null) {
        try {
          m_outputStream.close();
        }
        catch (IOException ex) {
        }
      }
      if (m_inputStream != null) {
        try {
          m_inputStream.close();
        }
        catch (IOException ex) {
        }
      }
      try {
        m_socket.close();
      }
      catch (IOException ex) {
      }
   }
  }

  protected void topLevel() throws Exception {
    System.out.println("Connect from " + m_socket.getRemoteSocketAddress());
    m_running = true;
    readMessage(false);
    processStartupMessage();
    while (m_running) {
      int type = readMessage();
      switch (type) {
      case -1:                  // EOF
        m_running = false;
        break;
      case PASSWORD_MESSAGE_TYPE:
        processPasswordMessage();
        break;
      case PARSE_TYPE:
        processParse();
        break;
      case BIND_TYPE:
        processBind();
        break;
      case DESCRIBE_TYPE:
        processDescribe();
        break;
      case EXECUTE_TYPE:
        processExecute();
        break;
      case SYNC_TYPE:
        processSync();
        break;
      case TERMINATE_TYPE:
        processTerminate();
        break;
      default:
        throw new Exception("Unknown message type: " + (char)type);
      }
    }
  }

  protected void processStartupMessage() throws Exception {
    int version = readInt();
    switch (version) {
    case VERSION_CANCEL:
      processCancelMessage();
      return;
    case VERSION_SSL:
      processSSLMessage();
      return;
    default:
      m_version = version;
      System.out.println("Version " + (version >> 16) + "." + (version & 0xFFFF));
    }
    m_properties = new Properties();
    while (true) {
      String param = readString();
      if (param.length() == 0) break;
      String value = readString();
      m_properties.put(param, value);
    }
    System.out.println(m_properties);
    String enc = m_properties.getProperty("client_encoding");
    if (enc != null) {
      if ("UNICODE".equals(enc))
        m_encoding = "UTF-8";
      else
        m_encoding = enc;
    }
    {
      beginMessage(AUTHENTICATION_TYPE);
      writeInt(AUTHENTICATION_CLEAR_TEXT);
      sendMessage(true);
    }
  }

  protected void processCancelMessage() throws Exception {
    int pid = readInt();
    int secret = readInt();
    PostgresServer pg = g_servers.get(pid);
    if ((pg != null) && (secret == pg.m_secret))
      // No easy way to signal in another thread.
      pg.m_cancel = true;
    m_running = false;
  }

  protected void processSSLMessage() throws Exception {
    throw new Exception("NIY");
  }

  protected void processPasswordMessage() throws Exception {
    String user = m_properties.getProperty("user");
    String pass = readString();
    System.out.println(user + "/" + pass);
    Properties status = new Properties();
    // This is enough to make the JDBC driver happy.
    status.put("client_encoding", m_properties.getProperty("client_encoding"));
    status.put("server_encoding", m_encoding);
    status.put("server_version", "8.4.7"); // Not sure what the min it'll accept is.
    status.put("session_authorization", user);
    
    {
      beginMessage(AUTHENTICATION_TYPE);
      writeInt(AUTHENTICATION_OK);
      sendMessage();
    }
    for (String prop : status.stringPropertyNames()) {
      beginMessage(PARAMETER_STATUS_TYPE);
      writeString(prop);
      writeString(status.getProperty(prop));
      sendMessage();
    }
    {
      beginMessage(BACKEND_KEY_DATA_TYPE);
      writeInt(m_pid);
      writeInt(m_secret);
      sendMessage();
    }
    {
      beginMessage(READY_FOR_QUERY_TYPE);
      writeByte('I');           // Idle ('T' -> xact open; 'E' -> xact abort)
      sendMessage(true);
    }
  }

  protected void processParse() throws Exception {
    String stmtName = readString();
    String query = readString();
    short nparams = readShort();
    int[] paramTypes = new int[nparams];
    for (int i = 0; i < nparams; i++)
      paramTypes[i] = readInt();
    m_preparedStatements.put(stmtName, new DummyPreparedStatement(query, paramTypes));
    beginMessage(PARSE_COMPLETE_TYPE);
    sendMessage();
  }

  protected void processBind() throws Exception {
    String portalName = readString();
    String stmtName = readString();
    short nformats = readShort();
    boolean[] paramsBinary = new boolean[nformats];
    for (int i = 0; i < nformats; i++)
      paramsBinary[i] = (readShort() == 1);
    short nparams = readShort();
    Object[] params = new Object[nparams];
    boolean binary = false;
    for (int i = 0; i < nparams; i++) {
      if (i < nformats)
        binary = paramsBinary[i];
      int len = readInt();
      if (len < 0) continue;    // Null
      byte[] param = new byte[len];
      readFully(param, 0, len);
      if (binary) {
        params[i] = param;
      }
      else {
        params[i] = new String(param, m_encoding);
      }
    }
    short nresults = readShort();
    boolean[] resultsBinary = new boolean[nresults];
    for (int i = 0; i < nresults; i++) {
      resultsBinary[i] = (readShort() == 1);
    }
    DummyPreparedStatement stmt = m_preparedStatements.get(stmtName);
    m_boundPortals.put(portalName, 
                       new DummyStatementBinding(stmt, params, resultsBinary));
    beginMessage(BIND_COMPLETE_TYPE);
    sendMessage();
  }

  protected void processDescribe() throws Exception {
    byte source = readByte();
    String name = readString();
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
    beginMessage(ROW_DESCRIPTION_TYPE);
    int nfields = 2;
    String[] names = new String[] { "foo", "bar" };
    writeShort(nfields);
    boolean binary = false;
    for (int i = 0; i < nfields; i++) {
      if ((resultsBinary != null) && (i < resultsBinary.length))
        binary = resultsBinary[i];
      writeString(names[i]);
      writeInt(0);              // OID of table.
      writeShort(0);            // colno in table.
      writeInt(VARCHAR_TYPE_OID); // data type
      writeShort(-1);           // typlen
      writeInt(0);              // atttypmod
      writeShort(binary ? 0 : 1);
    }
    sendMessage();
  }

  protected void processExecute() throws Exception {
    String portalName = readString();
    int maxrows = readInt();
    DummyStatementBinding binding = m_boundPortals.get(portalName);
    int nrows = 3;
    if (nrows == 0) {
      beginMessage(NO_DATA_TYPE);
      sendMessage();
    }
    else {
      if ((maxrows > 0) && (maxrows < nrows))
        nrows = maxrows;
      short ncols = 2;
      for (int j = 0; j < nrows; j++) {
        beginMessage(DATA_ROW_TYPE);
        writeShort(ncols);
        boolean binary = false;
        for (int i = 0; i < ncols; i++) {
          if (i < binding.m_resultsBinary.length)
            binary = binding.m_resultsBinary[i];
          String value = (i < 1) ? Integer.toString(j) : null;
          int len = (value == null) ? -1 : value.length();
          byte[] bv = null;
          if ((len > 0) && !binary)
            bv = value.getBytes(m_encoding);
          writeInt(len);
          if (bv != null)
            write(bv);
        }
        sendMessage();
      }
    }
    beginMessage(COMMAND_COMPLETE_TYPE);
    writeString("SELECT");
    sendMessage();
  }

  protected void processSync() throws Exception {
    beginMessage(READY_FOR_QUERY_TYPE);
    writeByte('I');           // Idle ('T' -> xact open; 'E' -> xact abort)
    sendMessage(true);
  }

  protected void processTerminate() throws Exception {
    m_running = false;
  }

  protected int readMessage() throws IOException {
    return readMessage(true);
  }

  protected int readMessage(boolean hasType) throws IOException {
    int type;
    if (hasType)
      type = m_dataInput.read();
    else
      type = STARTUP_MESSAGE_TYPE;
    if (type < 0) 
      return type;              // EOF
    int len = m_dataInput.readInt();
    len -= 4;
    byte[] msg = new byte[len];
    m_dataInput.readFully(msg, 0, len);
    m_messageInput = new DataInputStream(new ByteArrayInputStream(msg));
    return type;
  }

  protected void beginMessage(int type) throws IOException {
    m_byteOutput = new ByteArrayOutputStream();
    m_messageOutput = new DataOutputStream(m_byteOutput);
    m_messageOutput.write(type);
    m_messageOutput.writeInt(0);
  }

  protected void sendMessage() throws IOException {
    sendMessage(false);
  }

  protected void sendMessage(boolean flush) throws IOException {
    m_messageOutput.flush();
    byte[] msg = m_byteOutput.toByteArray();
    int len = msg.length - 1;
    msg[1] = (byte)(len >> 24);
    msg[2] = (byte)(len >> 16);
    msg[3] = (byte)(len >> 8);
    msg[4] = (byte)len;
    m_outputStream.write(msg);
    if (flush) m_outputStream.flush();
  }

  /** Read null-terminated string. */
  public String readString() throws IOException {
    ByteArrayOutputStream bs = new ByteArrayOutputStream();
    while (true) {
      int b = m_messageInput.read();
      if (b < 0) throw new IOException("EOF in the middle of a string");
      if (b == 0) break;
      bs.write(b);
    }
    return bs.toString(m_encoding);
  }

  /** Write null-terminated string. */
  public void writeString(String s) throws IOException {
    byte[] ba = s.getBytes(m_encoding);
    m_messageOutput.write(ba);
    m_messageOutput.write(0);
  }

  /*** DataInput ***/
  public boolean readBoolean() throws IOException {
    return m_messageInput.readBoolean();
  }
  public byte readByte() throws IOException {
    return m_messageInput.readByte();
  }
  public char readChar() throws IOException {
    return m_messageInput.readChar();
  }
  public double readDouble() throws IOException {
    return m_messageInput.readDouble();
  }
  public float readFloat() throws IOException {
    return m_messageInput.readFloat();
  }
  public void readFully(byte[] b) throws IOException {
    m_messageInput.readFully(b);
  }
  public void readFully(byte[] b, int off, int len) throws IOException {
    m_messageInput.readFully(b, off, len);
  }
  public int readInt() throws IOException {
    return m_messageInput.readInt();
  }
  @SuppressWarnings("deprecation")
  public String readLine() throws IOException {
    return m_messageInput.readLine();
  }
  public long readLong() throws IOException {
    return m_messageInput.readLong();
  }
  public short readShort() throws IOException {
    return m_messageInput.readShort();
  }
  public String readUTF() throws IOException {
    return m_messageInput.readUTF();
  }
  public int readUnsignedByte() throws IOException {
    return m_messageInput.readUnsignedByte();
  }
  public int readUnsignedShort() throws IOException {
    return m_messageInput.readUnsignedShort();
  }
  public int skipBytes(int n) throws IOException {
    return m_messageInput.skipBytes(n);
  }

  /*** DataOutput ***/
  public void write(byte[] data) throws IOException {
    m_messageOutput.write(data);
  }
  public void write(byte[] data, int ofs, int len) throws IOException {
    m_messageOutput.write(data, ofs, len);
  }
  public void write(int v) throws IOException {
    m_messageOutput.write(v);
  }
  public void writeBoolean(boolean v) throws IOException {
    m_messageOutput.writeBoolean(v);
  }
  public void writeByte(int v) throws IOException {
    m_messageOutput.writeByte(v);
  }
  public void writeBytes(String s) throws IOException {
    m_messageOutput.writeBytes(s);
  }
  public void writeChar(int v) throws IOException {
    m_messageOutput.writeChar(v);
  }
  public void writeChars(String s) throws IOException {
    m_messageOutput.writeChars(s);
  }
  public void writeDouble(double v) throws IOException {
    m_messageOutput.writeDouble(v);
  }
  public void writeFloat(float v) throws IOException {
    m_messageOutput.writeFloat(v);
  }
  public void writeInt(int v) throws IOException {
    m_messageOutput.writeInt(v);
  }
  public void writeLong(long v) throws IOException {
    m_messageOutput.writeLong(v);
  }
  public void writeShort(int v) throws IOException {
    m_messageOutput.writeShort(v);
  }
  public void writeUTF(String s) throws IOException {
    m_messageOutput.writeUTF(s);
  }
}
