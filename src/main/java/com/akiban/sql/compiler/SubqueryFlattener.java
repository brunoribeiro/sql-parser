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

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.impl.sql.compile.SubqueryNode

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.akiban.sql.compiler;

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.types.DataTypeDescriptor;
import com.akiban.sql.types.TypeId;

import java.util.Stack;

/** Flatten subqueries.
 *
 * This presently only handles a subset of the cases that Derby did,
 * just to get a feel for what is involved.
 */
public class SubqueryFlattener
{
  SQLParserContext parserContext;
  NodeFactory nodeFactory;
  public SubqueryFlattener(SQLParserContext parserContext) {
    this.parserContext = parserContext;
    this.nodeFactory = parserContext.getNodeFactory();
  }

  private Stack<SelectNode> selectStack;
  private SelectNode currentSelectNode;

  /** Flatten top-level statement.
   * Expects boolean predicates to already be in CNF.
   * 
   * Requires AIS bindings to already be in place. This is for the
   * uniqueness test. Another approach would be to always put into
   * some kind of join that might need special EXISTS processing (like
   * Derby's) when it actually gets generated.
   */
  public StatementNode flatten(StatementNode stmt) throws StandardException {
    if (stmt.getNodeType() == NodeTypes.CURSOR_NODE) {
      ResultSetNode resultSet = ((CursorNode)stmt).getResultSetNode();
      if (resultSet.getNodeType() == NodeTypes.SELECT_NODE) {
        selectStack = new Stack<SelectNode>();
        currentSelectNode = null;
        selectNode((SelectNode)resultSet);
      }
    }
    return stmt;
  }

  protected void selectNode(SelectNode selectNode) throws StandardException {
    selectStack.push(currentSelectNode);
    currentSelectNode = selectNode;
    // After CFN, only possibilities are AND and nothing.
    if (selectNode.getWhereClause() != null) {
      AndNode andNode = (AndNode)selectNode.getWhereClause();
      andNode(andNode);
    }
    currentSelectNode = selectStack.pop();
  }
  
  // Top-level (within some WHERE clause) AND expression.
  protected void andNode(AndNode andNode) throws StandardException {
    // Left operand might be IN (SELECT ...) or = ANY (SELECT ...)
    ValueNode leftOperand = andNode.getLeftOperand();
    if (leftOperand instanceof SubqueryNode) {
      leftOperand = subqueryNode((SubqueryNode)leftOperand, null);
    }
    else if (leftOperand instanceof BinaryComparisonOperatorNode) {
      BinaryComparisonOperatorNode bc = (BinaryComparisonOperatorNode)leftOperand;
      if (bc.getRightOperand() instanceof SubqueryNode)
        leftOperand = subqueryNode((SubqueryNode)bc.getRightOperand(), bc);
    }
    andNode.setLeftOperand(leftOperand);

    // Right operand is either another AND or constant TRUE.
    if (!andNode.getRightOperand().isBooleanTrue())
      andNode((AndNode)andNode.getRightOperand());
  }

  // Subquery either on RHS of binary comparison or as top-level boolean in WHERE.
  protected ValueNode subqueryNode(SubqueryNode subqueryNode, 
                                   BinaryComparisonOperatorNode parentComparisonOperator)
      throws StandardException {
    ValueNode result = parentComparisonOperator;
    if (result == null)
      result = subqueryNode;

    ResultSetNode resultSet = subqueryNode.getResultSet();
    // Must be simple SELECT
    if (!(resultSet instanceof SelectNode) ||
        (subqueryNode.getOrderByList() != null) ||
        (subqueryNode.getOffset() != null) ||
        (subqueryNode.getFetchFirst() != null))
      return result;

    // Either comparison or IN, EXISTS or ANY (i.e., not ALL or EXPRESSION).
    if (parentComparisonOperator == null) {
      switch (subqueryNode.getSubqueryType()) {
      case IN:
      case EXISTS:
      case EQ_ANY:
      case NE_ANY:
      case GT_ANY:
      case GE_ANY:
      case LT_ANY:
      case LE_ANY:
        break;
      default:
        return result;
      }
    }

    SelectNode selectNode = (SelectNode)resultSet;
    // Process sub-subqueries first.
    selectNode(selectNode);

    // And if any of those survive, give up.
    HasNodeVisitor visitor = new HasNodeVisitor(SubqueryNode.class);
    selectNode.accept(visitor);
    if (visitor.hasNode())
      return result;
    
    // Get left operand, if any (if from comparison, subquery is the right).
    ValueNode leftOperand = subqueryNode.getLeftOperand();
    if (parentComparisonOperator != null)
      leftOperand = parentComparisonOperator.getLeftOperand();
    
    boolean additionalEQ = false;
    switch (subqueryNode.getSubqueryType()) {
    case IN:
    case EQ_ANY:
      additionalEQ = true;
      break;
    }
    additionalEQ = additionalEQ && ((leftOperand instanceof ConstantNode) ||
                                    (leftOperand instanceof ColumnReference) ||
                                    (leftOperand instanceof ParameterNode));
    
    if (!isUniqueSubquery(selectNode, additionalEQ))
      return result;

    // Yes, we can flatten it.
    currentSelectNode.getFromList().addAll(selectNode.getFromList());
    currentSelectNode.setWhereClause(mergeWhereClause(currentSelectNode.getWhereClause(),
                                                      selectNode.getWhereClause()));
    if (leftOperand == null)
      return (ValueNode)nodeFactory.getNode(NodeTypes.BOOLEAN_CONSTANT_NODE,
                                            Boolean.TRUE,
                                            parserContext);

    // Right operand is whatever subquery select's.
    ValueNode rightOperand = 
      selectNode.getResultColumns().get(0).getExpression();
    int nodeType = 0;
    switch (subqueryNode.getSubqueryType()) {
      // TODO: The ALL and NOT_IN cases aren't actually supported here yet.
    case IN:
    case EQ_ANY:
    case NOT_IN:
    case NE_ALL:
      nodeType = NodeTypes.BINARY_EQUALS_OPERATOR_NODE;
      break;

    case NE_ANY:
    case EQ_ALL:
      nodeType = NodeTypes.BINARY_NOT_EQUALS_OPERATOR_NODE;
      break;

    case LE_ANY:
    case GT_ALL:
      nodeType = NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE;
      break;

    case LT_ANY:
    case GE_ALL:
      nodeType = NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE;
      break;

    case GE_ANY:
    case LT_ALL:
      nodeType = NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE;
      break;

    case GT_ANY:
    case LE_ALL:
      nodeType = NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE;
      break;

    default:
      assert false;
    }
    return (ValueNode)nodeFactory.getNode(nodeType,
                                          leftOperand, rightOperand, 
                                          parserContext);
  }

  protected boolean isUniqueSubquery(SelectNode selectNode, boolean additionalEQ)
      throws StandardException {
    return true;
  }

  protected ValueNode mergeWhereClause(ValueNode whereClause, ValueNode intoWhereClause)
      throws StandardException {
    if (intoWhereClause == null)
      return whereClause;
    assert (intoWhereClause instanceof AndNode);
    if (whereClause == null)
      return intoWhereClause;
    AndNode parentNode = (AndNode)intoWhereClause;
    while (true) {
      ValueNode rightOperand = parentNode.getRightOperand();
      if (rightOperand.isBooleanTrue())
        break;
      parentNode = (AndNode)rightOperand;
    }
    parentNode.setRightOperand(whereClause);
    return intoWhereClause;
  }
}
