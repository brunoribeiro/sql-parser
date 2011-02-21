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
import com.akiban.sql.types.DataTypeDescriptor;
import com.akiban.sql.types.TypeId;

import java.util.*;

/** Calculate types from schema information. */
public class TypeComputer implements Visitor
{
  public TypeComputer() {
  }

  public void compute(StatementNode stmt) throws StandardException {
    stmt.accept(this);
  }
  
  protected DataTypeDescriptor computeType(ValueNode node) throws StandardException {
    switch (node.getNodeType()) {
    case NodeTypes.COLUMN_REFERENCE:
      return columnReference((ColumnReference)node);
    case NodeTypes.RESULT_COLUMN:
      return resultColumn((ResultColumn)node);
    default:
      // assert false;
      return null;
    }
  }

  protected DataTypeDescriptor columnReference(ColumnReference node) 
      throws StandardException {
    ColumnBinding columnBinding = (ColumnBinding)node.getUserData();
    assert (columnBinding != null) : "column is not bound yet";
    return columnBinding.getType();
  }

  protected DataTypeDescriptor resultColumn(ResultColumn node)
      throws StandardException {
    return node.getExpression().getType();
  }

  protected void selectNode(SelectNode node) throws StandardException {
    // Children first wasn't enough to ensure that subqueries were done first.
    node.getResultColumns().accept(this);
  }

  /* Visitor interface. */

  public Visitable visit(Visitable node) throws StandardException {
    if (node instanceof ValueNode) {
      // Value nodes compute type if necessary.
      ValueNode valueNode = (ValueNode)node;
      if (valueNode.getType() == null) {
        valueNode.setType(computeType(valueNode));
      }
    }
    else {
      // Some structural nodes require special handling.
      switch (((QueryTreeNode)node).getNodeType()) {
      case NodeTypes.SELECT_NODE:
        selectNode((SelectNode)node);
      }
    }
    return node;
  }
  
  public boolean skipChildren(Visitable node) throws StandardException {
    return false;
  }
  public boolean visitChildrenFirst(Visitable node) {
    return true;
  }
  public boolean stopTraversal() {
    return false;
  }

}
