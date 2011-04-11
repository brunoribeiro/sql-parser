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
import com.akiban.qp.physicaloperator.PhysicalOperator;
import com.akiban.qp.rowtype.RowType;
import com.akiban.server.service.session.Session;

import java.util.*;
import java.io.IOException;

/**
 * An SQL SELECT transformed into an operator tree
 * @see PostgresHapiCompiler
 */
public class PostgresOperatorStatement extends PostgresStatement
{
  private PhysicalOperator m_resultOperator;
  private RowType m_resultRowType;
  private int[] m_resultColumnOffsets;

  public PostgresOperatorStatement(PhysicalOperator resultOperator,
                                   RowType resultRowType,
                                   List<Column> resultColumns,
                                   int[] resultColumnOffsets) {
    super(resultColumns);
    m_resultOperator = resultOperator;
    m_resultRowType = resultRowType;
    m_resultColumnOffsets = resultColumnOffsets;
  }
  
  public int execute(PostgresMessenger messenger, Session session, int maxrows)
      throws IOException, StandardException {
    return 0;
  }

}
