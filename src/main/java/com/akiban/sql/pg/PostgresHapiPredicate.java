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
import com.akiban.server.api.HapiPredicate;

public class PostgresHapiPredicate implements HapiPredicate
{
  private TableName m_tableName;
  private String m_columnName;
  private Operator m_op;
  private String m_value;
  private int m_parameterIndex;

  public PostgresHapiPredicate(TableName tableName, String columnName,
                               Operator op, String value) {
    m_tableName = tableName;
    m_columnName = columnName;
    m_op = op;
    m_value = value;
  }

  public PostgresHapiPredicate(TableName tableName, String columnName,
                               Operator op, int parameterIndex) {
    m_tableName = tableName;
    m_columnName = columnName;
    m_op = op;
    m_parameterIndex = parameterIndex;
  }

  public PostgresHapiPredicate(PostgresHapiPredicate other, String value) {
    m_tableName = other.m_tableName;
    m_columnName = other.m_columnName;
    m_op = other.m_op;
    m_value = value;
  }
                               
  public TableName getTableName() {
    return m_tableName;
  }

  public String getColumnName() {
    return m_columnName;
  }

  public Operator getOp() {
    return m_op;
  }

  public String getValue() {
    return m_value;
  }

  public int getParameterIndex() {
    return m_parameterIndex;
  }
}
