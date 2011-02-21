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

/** Calculate types in statement tree. 
 * Called after schema binding has introduced column types.
 *
 * Most of the work is done by the ValueNodes themselves, since data
 * type information is intrinsic to the AST.
 */
public class TypeComputer implements Visitor
{
  public TypeComputer() {
  }

  public void compute(StatementNode stmt) throws StandardException {
    stmt.accept(this);
  }

  protected void selectNode(SelectNode node) throws StandardException {
    // Children first wasn't enough to ensure that subqueries were done first.
    node.getResultColumns().accept(this);
  }

  protected DataTypeDescriptor computeType(ValueNode node) throws StandardException {
    // Only those that depend upon our binding scheme need to be handled by us.
    switch (node.getNodeType()) {
    case NodeTypes.COLUMN_REFERENCE:
      return columnReference((ColumnReference)node);
    default:
      return node.computeType();
    }
  }
 
  protected DataTypeDescriptor columnReference(ColumnReference node) 
      throws StandardException {
    ColumnBinding columnBinding = (ColumnBinding)node.getUserData();
    assert (columnBinding != null) : "column is not bound yet";
    return columnBinding.getType();
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
