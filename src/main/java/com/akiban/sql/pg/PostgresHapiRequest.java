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
import com.akiban.ais.model.TableName;
import com.akiban.ais.model.UserTable;
import com.akiban.server.api.HapiGetRequest;
import com.akiban.server.api.HapiPredicate;

import java.util.*;

/**
 * An SQL SELECT transformed into a Hapi request.
 * @see PostgresHapiCompiler
 */
public class PostgresHapiRequest implements HapiGetRequest
{
  private UserTable m_shallowestTable, m_queryTable, m_deepestTable;
  private List<HapiPredicate> m_predicates; // All on m_queryTable.
  private List<Column> m_columns; // Any from m_shallowestTable through m_deepestTable.
  private boolean[] m_columnBinary; // Is this column binary format?
  private boolean m_defaultColumnBinary;

  public PostgresHapiRequest(UserTable shallowestTable, UserTable queryTable, 
                             UserTable deepestTable,
                             List<HapiPredicate> predicates, List<Column> columns,
                             boolean[] columnBinary, boolean defaultColumnBinary) {
    m_shallowestTable = shallowestTable;
    m_queryTable = queryTable;
    m_deepestTable = deepestTable;
    m_predicates = predicates;
    m_columns = columns;
    m_columnBinary = columnBinary;
    m_defaultColumnBinary = defaultColumnBinary;
  }

  public UserTable getShallowestTable() {
    return m_shallowestTable;
  }
  public UserTable getQueryTable() {
    return m_queryTable;
  }
  public UserTable getDeepestTable() {
    return m_deepestTable;
  }

  public List<Column> getColumns() {
    return m_columns;
  }

  public boolean isColumnBinary(int i) {
    if ((m_columnBinary != null) && (i < m_columnBinary.length))
      return m_columnBinary[i];
    else
      return m_defaultColumnBinary;
  }

  /*** HapiGetRequest ***/

  /**
   * The name of the schema containing the tables involved in this request. Matches getUsingTable().getSchemaName().
   * @return The name of the schema containing the tables involved in this request.
   */
  public String getSchema() {
    return m_shallowestTable.getName().getSchemaName();
  }
  
  /**
   * Rootmost table to be retrieved by this request.
   * @return The name (without schema) of the rootmost table to be retrieved.
   */
  public String getTable() {
    return m_shallowestTable.getName().getTableName();
  }

  /**
   * The table whose columns are restricted by this request.
   * @return The schema and table name of the table whose columns are restricted by this request.
   */
  public TableName getUsingTable() {
    return m_queryTable.getName();
  }

  public int getLimit() {
    return -1;
  }

  public List<HapiPredicate> getPredicates() {
    return m_predicates;
  }
}
