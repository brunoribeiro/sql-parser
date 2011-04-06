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

import com.akiban.ais.model.Column;
import com.akiban.server.service.session.Session;

import java.util.*;
import java.io.IOException;

/**
 * An SQL statement compiled for use with Postgres server.
 * @see PostgresStatementCompiler
 */
public abstract class PostgresStatement
{
  private List<Column> m_columns; // Any from m_shallowestTable through m_deepestTable.
  private List<PostgresType> m_types;

  public PostgresStatement(List<Column> columns) {
    m_columns = columns;
  }

  public List<Column> getColumns() {
    return m_columns;
  }

  public boolean isColumnBinary(int i) {
    return false;
  }

  public List<PostgresType> getTypes() throws StandardException {
    if (m_types == null) {
      m_types = new ArrayList<PostgresType>(m_columns.size());
      for (Column column : m_columns) {
        m_types.add(PostgresType.fromAIS(column));
      }
    }
    return m_types;
  }

  public void sendRowDescription(PostgresMessenger messenger) 
      throws IOException, StandardException {
    messenger.beginMessage(PostgresMessenger.ROW_DESCRIPTION_TYPE);
    List<Column> columns = getColumns();
    List<PostgresType> types = getTypes();
    int ncols = columns.size();
    messenger.writeShort(ncols);
    for (int i = 0; i < ncols; i++) {
      Column col = columns.get(i);
      PostgresType type = types.get(i);
      messenger.writeString(col.getName()); // attname
      messenger.writeInt(0);              // attrelid
      messenger.writeShort(0);            // attnum
      messenger.writeInt(type.getOid()); // atttypid
      messenger.writeShort(type.getLength()); // attlen
      messenger.writeInt(type.getModifier()); // atttypmod
      messenger.writeShort(isColumnBinary(i) ? 1 : 0);
    }
    messenger.sendMessage();
  }
  
  public PostgresStatement getBoundRequest(String[] parameters,
                                           boolean[] columnBinary, 
                                           boolean defaultColumnBinary) {
    return this;
  }

  public abstract int execute(PostgresMessenger messenger, Session session, int maxrows)
      throws IOException, StandardException;

}
