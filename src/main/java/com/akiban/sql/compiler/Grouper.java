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

package com.akiban.sql.compiler;

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;

import com.akiban.ais.model.Column;
import com.akiban.ais.model.Group;
import com.akiban.ais.model.GroupTable;
import com.akiban.ais.model.Join;
import com.akiban.ais.model.JoinColumn;
import com.akiban.ais.model.Table;

import java.util.*;

/** Match joined tables to groups. */
public class Grouper implements Visitor
{
  enum VisitMode { GROUP, REWRITE };
  VisitMode visitMode;

  SQLParserContext parserContext;
  NodeFactory nodeFactory;
  public Grouper(SQLParserContext parserContext) {
    this.parserContext = parserContext;
    this.nodeFactory = parserContext.getNodeFactory();
  }

  public void group(StatementNode stmt) throws StandardException {
    visitMode = VisitMode.GROUP;
    stmt.accept(this);
    visitMode = null;
  }
  
  public void rewrite(StatementNode stmt) throws StandardException {
    visitMode = VisitMode.REWRITE;
    stmt.accept(this);
    visitMode = null;
  }
  
  /* Group finding */

  protected void groupSelectNode(SelectNode selectNode) throws StandardException {
    
  }

  /* Group rewriting */

  protected QueryTreeNode rewriteNode(QueryTreeNode node) throws StandardException {
    switch (node.getNodeType()) {
    default:
      break;
    }
    return node;
  }

  /* Visitor interface */

  public Visitable visit(Visitable node) throws StandardException {
    switch (visitMode) {
    case GROUP:
      switch (((QueryTreeNode)node).getNodeType()) {
      case NodeTypes.SELECT_NODE:
        groupSelectNode((SelectNode)node);
        break;
      }
      break;
    case REWRITE:
      return rewriteNode((QueryTreeNode)node);
    default:
      assert false : "Invalid visit mode";
    }
    return node;
  }

  public boolean visitChildrenFirst(Visitable node) {
    return false;
  }
  public boolean stopTraversal() {
    return false;
  }
  public boolean skipChildren(Visitable node) throws StandardException {
    return false;
  }

}
