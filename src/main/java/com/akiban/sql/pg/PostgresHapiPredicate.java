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
import com.akiban.server.api.HapiPredicate;

public class PostgresHapiPredicate implements HapiPredicate
{
  private Column m_column;
  private Operator m_op;
  private String m_value;
  private int m_parameterIndex;

  public PostgresHapiPredicate(Column column, Operator op, String value) {
    m_column = column;
    m_op = op;
    m_value = value;
    m_parameterIndex = -1;
  }

  public PostgresHapiPredicate(Column column,
                               Operator op, int parameterIndex) {
    m_column = column;
    m_op = op;
    m_parameterIndex = parameterIndex;
  }

  public PostgresHapiPredicate(PostgresHapiPredicate other, String value) {
    m_column = other.m_column;
    m_op = other.m_op;
    m_value = value;
    m_parameterIndex = -1;
  }
                               
  public TableName getTableName() {
    return m_column.getUserTable().getName();
  }

  public String getColumnName() {
    return m_column.getName();
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
