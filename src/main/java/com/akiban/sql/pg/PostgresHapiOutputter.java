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

import com.akiban.ais.model.Column;
import com.akiban.ais.model.UserTable;
import com.akiban.server.RowData;
import com.akiban.server.RowDef;
import com.akiban.server.api.HapiOutputter;
import com.akiban.server.api.HapiProcessedGetRequest;
import com.akiban.server.api.HapiRequestException;
import com.akiban.server.api.dml.scan.LegacyRowWrapper;
import com.akiban.server.api.dml.scan.NewRow;
import com.akiban.server.service.memcache.hprocessor.Scanrows;
import com.akiban.server.service.session.Session;
import com.akiban.server.service.session.SessionImpl;

import java.io.*;
import java.util.*;

public class PostgresHapiOutputter implements HapiOutputter {
  private PostgresServer m_server;
  private PostgresHapiRequest m_request;
  private Session m_session;

  public PostgresHapiOutputter(PostgresServer server) {
    m_server = server;
    m_session = new SessionImpl();
  }

  public void run(Session session, PostgresHapiRequest request) 
      throws HapiRequestException {
    m_request = request;
    Scanrows.instance().processRequest(m_session, request, this, null);
  }

  int m_ncols;
  int[] m_tableIds, m_columnIds;
  NewRow[] m_rows;

  public void output(HapiProcessedGetRequest request, Iterable<RowData> rows, 
              OutputStream outputStream) throws IOException {
    List<Column> columns = m_request.getColumns();
    m_ncols = columns.size();
    m_tableIds = new int[m_ncols];
    m_columnIds = new int[m_ncols];
    for (int i = 0; i < m_ncols; i++) {
      m_tableIds[i] = m_columnIds[i] = -1;
    }
    int ntables = 256;          // TODO: From where?
    m_rows = new NewRow[ntables];
    boolean[] processedTableIds = new boolean[ntables];
    int queryTableId = -1;
    for (RowData rowData : rows) {
      NewRow row = new LegacyRowWrapper(rowData).niceRow();
      int tableId = row.getTableId();
      if (!processedTableIds[tableId]) {
        RowDef rowDef = row.getRowDef();
        UserTable table = rowDef.userTable();
        if (table.getName().equals(request.getUsingTable()))
          queryTableId = tableId;
        for (int i = 0; i < m_ncols; i++) {
          Column column = columns.get(i);
          if (column.getTable().equals(table)) {
            m_tableIds[i] = tableId;
            m_columnIds[i] = column.getPosition();
          }
        }
        processedTableIds[tableId] = true;
      }
    }
  }

  // Send the current row, whose columns may come from ancestors.
  protected void sendDataRow() throws IOException {
    m_server.beginMessage(PostgresServer.DATA_ROW_TYPE);
    m_server.writeShort(m_ncols);
    boolean binary = false;
    for (int i = 0; i < m_ncols; i++) {
      Object value = null;
      int fromTableId = m_tableIds[i];
      if (fromTableId != -1) {
        NewRow fromRow = m_rows[fromTableId];
        if (fromRow != null) {
          value = fromRow.get(m_columnIds[i]);
        }
      }
      if (value == null) {
        m_server.writeInt(-1);
      }
      else {
        byte[] bv;
        if (binary) {
          bv = null;            // TODO: Figure out encodings.
        }
        else {
          bv = value.toString().getBytes(m_server.getEncoding());
        }
        m_server.writeInt(bv.length);
        m_server.write(bv);
      }
    }
    m_server.sendMessage();
  }
}
