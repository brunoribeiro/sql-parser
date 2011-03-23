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

import java.io.*;
import java.util.*;

/**
 * Basic implementation of Postgres wire protocol for SQL integration.
 *
 * See http://developer.postgresql.org/pgdocs/postgres/protocol.html
 */
public class PostgresMessenger implements DataInput, DataOutput
{
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

  private InputStream m_inputStream;
  private OutputStream m_outputStream;
  private DataInputStream m_dataInput;
  private DataInputStream m_messageInput;
  private ByteArrayOutputStream m_byteOutput;
  private DataOutputStream m_messageOutput;
  private String m_encoding = "ISO-8859-1";
  private boolean m_cancel = false;

  public PostgresMessenger(InputStream inputStream, OutputStream outputStream) {
    m_inputStream = inputStream;
    m_outputStream = outputStream;
    m_dataInput = new DataInputStream(m_inputStream);
  }

  /** The encoding used for strings. */
  public String getEncoding() {
    return m_encoding;
  }
  public void setEncoding(String encoding) {
    m_encoding = encoding;
  }

  /** Has a cancel been sent? */
  public synchronized boolean isCancel() {
    return m_cancel;
  }
  /** Mark as cancelled. Cleared at the start of results. 
   * Usually set from a thread running a request just for that purpose. */
  public synchronized void setCancel(boolean cancel) {
    m_cancel = cancel;
  }

  /** Read the next message from the stream, without any type opcode. */
  protected int readMessage() throws IOException {
    return readMessage(true);
  }
  /** Read the next message from the stream, starting with the message type opcode. */
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

  /** Begin outgoing message of given type. */
  protected void beginMessage(int type) throws IOException {
    m_byteOutput = new ByteArrayOutputStream();
    m_messageOutput = new DataOutputStream(m_byteOutput);
    m_messageOutput.write(type);
    m_messageOutput.writeInt(0);
  }

  /** Send outgoing message. */
  protected void sendMessage() throws IOException {
    sendMessage(false);
  }
  /** Send outgoing message and optionally flush stream. */
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
