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
  public static final int AUTHENTICATION_OK_TYPE = 'R'; // (B)
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
  public static final int STARTUP_MESSAGE_TYPE = -1; // (F)
  public static final int SYNC_TYPE = 'S'; // (F)
  public static final int TERMINATE_TYPE = 'X'; // (F)
  
  public static final int VERSION_CANCEL = 80877102; // 12345678
  public static final int VERSION_SSL = 80877103; // 12345679
  
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
    m_messageOutput.flush();
    byte[] msg = m_byteOutput.toByteArray();
    int len = msg.length - 1;
    msg[1] = (byte)(len >> 24);
    msg[2] = (byte)(len >> 16);
    msg[3] = (byte)(len >> 8);
    msg[4] = (byte)len;
    m_outputStream.write(msg);
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
