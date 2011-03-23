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

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.compiler.AISBinder;
import com.akiban.sql.compiler.BindingNodeFactory;
import com.akiban.sql.compiler.BooleanNormalizer;
import com.akiban.sql.compiler.BoundNodeToString;
import com.akiban.sql.compiler.Grouper;
import com.akiban.sql.compiler.SubqueryFlattener;
import com.akiban.sql.compiler.TypeComputer;
import com.akiban.sql.views.ViewDefinition;

import com.akiban.ais.model.AkibanInformationSchema;

import java.util.*;


/**
 * Compile SQL SELECT statements into Hapi requests if possible.
 * Restrictions are:
 * <ul>
 * <li>All result columns directly from tables.</li>
 * <li>All FROM tables in a strict hierarchy with no branching.</li>
 * <li>WHERE or JOIN ON clause has necessary pkey-fkey equality conditions.</li>
 * <li>Remaining WHERE clause is simple boolean predicates between columns from a single table with constants.</li>
 * <li>No (unflattened) subqueries.</li>
 * <li>No ORDER BY.</li>
 * <li>No DISTINCT.</li>
 * <li>No GROUP BY.</li>
 * <li>No OFFSET or FETCH.</li>
 * <li>No WINDOW.</li>
 * <li>No FOR UPDATE.</li>
 * </ul>
 */
public class PostgresHapiCompiler
{
  private SQLParserContext m_parserContext;
  private NodeFactory m_nodeFactory;
  private AISBinder m_binder;
  private TypeComputer m_typeComputer;
  private BooleanNormalizer m_booleanNormalizer;
  private SubqueryFlattener m_subqueryFlattener;
  private Grouper m_grouper;

  public PostgresHapiCompiler(SQLParser parser, 
                              AkibanInformationSchema ais, String user) {
    m_parserContext = parser;
    m_nodeFactory = m_parserContext.getNodeFactory();
    m_binder = new AISBinder(ais, user);
    parser.setNodeFactory(new BindingNodeFactory(m_nodeFactory));
    m_typeComputer = new TypeComputer();
    m_booleanNormalizer = new BooleanNormalizer(parser);
    m_subqueryFlattener = new SubqueryFlattener(parser);
    m_grouper = new Grouper(parser);
  }

  public void addView(ViewDefinition view) throws StandardException {
    m_binder.addView(view);
  }

  public PostgresHapiRequest compile(CursorNode stmt) throws StandardException {
    // Get into bound & grouped form.
    m_binder.bind(stmt);
    stmt = (CursorNode)m_booleanNormalizer.normalize(stmt);
    m_typeComputer.compute(stmt);
    stmt = (CursorNode)m_subqueryFlattener.flatten(stmt);
    m_grouper.group(stmt);
    return null;
  }
}
