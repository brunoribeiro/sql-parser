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

import com.akiban.ais.model.TableName;
import com.akiban.server.api.HapiGetRequest;
import com.akiban.server.api.HapiPredicate;

import java.util.*;

public class PostgresHapiRequest implements HapiGetRequest
{
  private String m_schema, m_rootTable;
  private TableName m_queryTable;
  private List<HapiPredicate> m_predicates;

  public PostgresHapiRequest(String schema, String rootTable, TableName queryTable,
                             List<HapiPredicate> predicates) {
    m_schema = schema;
    m_rootTable = rootTable;
    m_queryTable = queryTable;
    m_predicates = predicates;
  }

  /*** HapiGetRequest ***/

  /**
   * The name of the schema containing the tables involved in this request. Matches getUsingTable().getSchemaName().
   * @return The name of the schema containing the tables involved in this request.
   */
  public String getSchema() {
    return m_schema;
  }
  
  /**
   * Rootmost table to be retrieved by this request.
   * @return The name (without schema) of the rootmost table to be retrieved.
   */
  public String getTable() {
    return m_rootTable;
  }

  /**
   * The table whose columns are restricted by this request.
   * @return The schema and table name of the table whose columns are restricted by this request.
   */
  public TableName getUsingTable() {
    return m_queryTable;
  }

  public int getLimit() {
    return -1;
  }

  public List<HapiPredicate> getPredicates() {
    return m_predicates;
  }
}
