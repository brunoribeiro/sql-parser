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

import java.net.*;
import java.io.*;
import java.util.*;

/** The PostgreSQL server service.
 * @see PostgresServer
 * 
 * <code>JVM_OPTS="-Dakserver.services.customload=com.akiban.sql.pg.PostgresServerManager" $AKIBAN_SERVER_HOME/bin/akserver -f -j /opt/akiban/source/parser-latest/parser-combined.jar</code>
*/
public class PostgresServerManager implements PostgresService, Service<PostgresService> {
  public static final int DEFAULT_PORT = 15432; // Real one is 5432
  
  private int m_port = DEFAULT_PORT;
  private ServerSocket m_socket = null;
  private boolean m_running = false;
  private Map<Integer,PostgresServer> m_servers =
      new HashMap<Integer,PostgresServer>();

  public PostgresServerManager() {
  }

  protected void topLevel() {
    System.out.println("Listening on port " + m_port);
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
        PostgresServer pg = new PostgresServer(this, socket, pid, secret);
        m_servers.put(pid, pg);
        new Thread(pg).start();
      }
    }
    catch (Exception ex) {
      if (m_running)
        ex.printStackTrace(System.err);
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

  public PostgresServer getServer(int pid) {
    return m_servers.get(pid);
  }

  public void removeServer(int pid) {
    m_servers.remove(pid);
  }

  /*** Service<PostgresService> ***/

  public PostgresService cast() {
    return this;
  }

  public Class<PostgresService> castClass() {
    return PostgresService.class;
  }

  public void start() throws Exception {
    m_running = true;
    new Thread() {
      public void run() {
        topLevel();
      }
    }.start();
  }

  public void stop() throws Exception {
    // Can only wake up by closing socket inside whose accept() we are blocked.
    ServerSocket socket;
    synchronized(this) {
      m_running = false;
      socket = m_socket;
    }
    if (socket != null) {
      try {
        m_socket.close();
      }
      catch (IOException ex) {
      }
    }

    for (PostgresServer server : m_servers.values()) {
      server.stop();
    }
  }

}
