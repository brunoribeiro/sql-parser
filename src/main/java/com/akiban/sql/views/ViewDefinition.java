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

package com.akiban.sql.views;

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.types.DataTypeDescriptor;

public class ViewDefinition
{
  private SQLParserContext parserContext;
  private NodeFactory nodeFactory;
  private CreateViewNode definition;

  /**
   * Parse the given SQL as CREATE VIEW and remember the definition.
   */
  public ViewDefinition(String sql, SQLParser parser)
      throws StandardException {
    this.parserContext = parser;
    this.nodeFactory = parserContext.getNodeFactory();

    QueryTreeNode parsed = parser.parseStatement(sql);
    if (parsed.getNodeType() != NodeTypes.CREATE_VIEW_NODE) {
      throw new StandardException("Parsed statement was not a view");
    }
    this.definition = (CreateViewNode)parsed;
  }

  /** 
   * Get the name of the view.
   */
  public TableName getName() throws StandardException {
    return definition.getObjectName();
  }

  private FromSubquery subquery = null;

  /**
   * Get the view as an equivalent subquery.
   */
  public FromSubquery getSubquery(Visitor binder) throws StandardException {
    if (subquery == null) {
      subquery = (FromSubquery)
        nodeFactory.getNode(NodeTypes.FROM_SUBQUERY,
                            definition.getParsedQueryExpression(),
                            null, null, null,
                            getName().getTableName(),
                            definition.getResultColumns(),
                            null,
                            parserContext);
      subquery = (FromSubquery)subquery.accept(binder);
    }
    // Return a clone so caller can mess with it and not affect us.
    return (FromSubquery)nodeFactory.copyNode(subquery, parserContext);
  }
}
