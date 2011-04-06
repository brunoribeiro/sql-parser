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
import com.akiban.sql.compiler.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.views.ViewDefinition;

import com.akiban.ais.model.AkibanInformationSchema;
import com.akiban.ais.model.Column;
import com.akiban.ais.model.Join;
import com.akiban.ais.model.UserTable;

import com.akiban.qp.persistitadapter.PersistitAdapter;
import com.akiban.qp.physicaloperator.StoreAdapter;
import com.akiban.qp.rowtype.Schema;

import com.akiban.server.service.ServiceManager;
import com.akiban.server.service.session.Session;
import com.akiban.server.store.PersistitStore;

import java.util.*;

/**
 * Compile SQL SELECT statements into operator trees if possible.
 */
public class PostgresOperatorCompiler implements PostgresStatementCompiler
{
  private SQLParserContext m_parserContext;
  private NodeFactory m_nodeFactory;
  private AISBinder m_binder;
  private TypeComputer m_typeComputer;
  private BooleanNormalizer m_booleanNormalizer;
  private SubqueryFlattener m_subqueryFlattener;
  private Grouper m_grouper;
  private StoreAdapter m_adapter;

  public PostgresOperatorCompiler(SQLParser parser, 
                                  AkibanInformationSchema ais, String schema,
                                  Session session, ServiceManager serviceManager) {
    m_parserContext = parser;
    m_nodeFactory = m_parserContext.getNodeFactory();
    m_binder = new AISBinder(ais, schema);
    parser.setNodeFactory(new BindingNodeFactory(m_nodeFactory));
    m_typeComputer = new TypeComputer();
    m_booleanNormalizer = new BooleanNormalizer(parser);
    m_subqueryFlattener = new SubqueryFlattener(parser);
    m_grouper = new Grouper(parser);
    m_adapter = new PersistitAdapter(new Schema(ais),
                                     (PersistitStore)serviceManager.getStore(),
                                     session);
  }

  public void addView(ViewDefinition view) throws StandardException {
    m_binder.addView(view);
  }

  @Override
  public PostgresStatement compile(CursorNode cursor, int[] paramTypes)
      throws StandardException {
    return null;
  }
}
