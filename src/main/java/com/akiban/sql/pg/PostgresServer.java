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

import com.akiban.server.service.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.io.*;
import java.util.*;

/** The PostgreSQL server.
 * Listens of a given port and spawns <code>PostgresServerConnection</code> threads
 * to process requests.
 * Also keeps global state for shutdown and inter-connection communication like cancel.
*/
public class PostgresServer implements Runnable {
  private static final Logger LOG = LoggerFactory.getLogger(PostgresServer.class);
  public static final int DEFAULT_PORT = 15432; // Real one is 5432
  
  private int m_port = DEFAULT_PORT;
  private ServerSocket m_socket = null;
  private boolean m_running = false;
  private Map<Integer,PostgresServerConnection> m_connections =
      new HashMap<Integer,PostgresServerConnection>();

  public PostgresServer(int port) {
    m_port = port;
  }

  /** Called from the (AkServer's) main thread to start a server
      running in its own thread. */
  public void start() {
    m_running = true;
    new Thread(this).start();
  }

  /** Called from the main thread to shutdown a server. */
  public void stop() {
    ServerSocket socket;
    synchronized(this) {
      // Service might shutdown before we've even got server socket created.
      m_running = false;
      socket = m_socket;
    }
    if (socket != null) {
      // Can only wake up by closing socket inside whose accept() we are blocked.
      try {
        socket.close();
      }
      catch (IOException ex) {
      }
    }

    for (PostgresServerConnection connection : m_connections.values()) {
      connection.stop();
    }
  }

  public void run() {
    LOG.warn("Postgres server listening on port {}", m_port);
    int pid = 0;
    Random rand = new Random();
    try {
      synchronized(this) {
        if (!m_running) return;
        m_socket = new ServerSocket(m_port);
      }
      while (m_running) {
        Socket socket = m_socket.accept();
        pid++;
        int secret = rand.nextInt();
        PostgresServerConnection connection = 
          new PostgresServerConnection(this, socket, pid, secret);
        m_connections.put(pid, connection);
        connection.start();
      }
    }
    catch (Exception ex) {
      if (m_running)
        LOG.warn("Error in server", ex);
    }
    finally {
      if (m_socket != null) {
        try {
          m_socket.close();
        }
        catch (IOException ex) {
        }
      }
      m_running = false;
    }
  }

  public PostgresServerConnection getConnection(int pid) {
    return m_connections.get(pid);
  }
  public void removeConnection(int pid) {
    m_connections.remove(pid);
  }

}
