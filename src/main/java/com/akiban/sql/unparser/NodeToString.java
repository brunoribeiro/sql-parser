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

package com.akiban.sql.unparser;

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.types.DataTypeDescriptor;

public class NodeToString
{
  public NodeToString() {
  }

  public String toString(QueryTreeNode node) throws StandardException {
    switch (node.getNodeType()) {
    case NodeTypes.CURSOR_NODE:
      return cursorNode((CursorNode)node);
    case NodeTypes.SELECT_NODE:
      return selectNode((SelectNode)node);
    case NodeTypes.INSERT_NODE:
      return insertNode((InsertNode)node);
    case NodeTypes.UPDATE_NODE:
      return updateNode((UpdateNode)node);
    case NodeTypes.DELETE_NODE:
      return deleteNode((DeleteNode)node);
    case NodeTypes.RESULT_COLUMN_LIST:
      return resultColumnList((ResultColumnList)node);
    case NodeTypes.RESULT_COLUMN:
      return resultColumn((ResultColumn)node);
    case NodeTypes.FROM_LIST:
      return fromList((FromList)node);
    case NodeTypes.GROUP_BY_LIST:
      return groupByList((GroupByList)node);
    case NodeTypes.ORDER_BY_LIST:
      return orderByList((OrderByList)node);
    case NodeTypes.VALUE_NODE_LIST:
      return valueNodeList((ValueNodeList)node);
    case NodeTypes.FROM_BASE_TABLE:
      return fromBaseTable((FromBaseTable)node);
    case NodeTypes.TABLE_NAME:
      return tableName((TableName)node);
    case NodeTypes.COLUMN_REFERENCE:
      return columnReference((ColumnReference)node);
    case NodeTypes.ROW_RESULT_SET_NODE:
      return rowResultSetNode((RowResultSetNode)node);
    case NodeTypes.GROUP_BY_COLUMN:
      return groupByColumn((GroupByColumn)node);
    case NodeTypes.ORDER_BY_COLUMN:
      return orderByColumn((OrderByColumn)node);
    case NodeTypes.AND_NODE:
    case NodeTypes.OR_NODE:
      return binaryLogicalOperatorNode((BinaryLogicalOperatorNode)node);
    case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
    case NodeTypes.BINARY_NOT_EQUALS_OPERATOR_NODE:
    case NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE:
    case NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE:
    case NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE:
    case NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE:
      return binaryComparisonOperatorNode((BinaryComparisonOperatorNode)node);
    case NodeTypes.BINARY_PLUS_OPERATOR_NODE:
    case NodeTypes.BINARY_TIMES_OPERATOR_NODE:
    case NodeTypes.BINARY_DIVIDE_OPERATOR_NODE:
    case NodeTypes.BINARY_MINUS_OPERATOR_NODE:
      return binaryArithmeticOperatorNode((BinaryArithmeticOperatorNode)node);
    case NodeTypes.CONCATENATION_OPERATOR_NODE:
      return concatenationOperatorNode((ConcatenationOperatorNode)node);
    case NodeTypes.NOT_NODE:
      return notNode((NotNode)node);
    case NodeTypes.IS_NULL_NODE:
    case NodeTypes.IS_NOT_NULL_NODE:
      return isNullnode((IsNullNode)node);
    case NodeTypes.LIKE_OPERATOR_NODE:
      return likeEscapeOperatorNode((LikeEscapeOperatorNode)node);
    case NodeTypes.IN_LIST_OPERATOR_NODE:
      return inListOperatorNode((InListOperatorNode)node);
    case NodeTypes.BETWEEN_OPERATOR_NODE:
      return betweenOperatorNode((BetweenOperatorNode)node);
    case NodeTypes.AGGREGATE_NODE:
      return aggregateNode((AggregateNode)node);
    case NodeTypes.UNTYPED_NULL_CONSTANT_NODE:
    case NodeTypes.SQL_BOOLEAN_CONSTANT_NODE:
    case NodeTypes.BOOLEAN_CONSTANT_NODE:
    case NodeTypes.BIT_CONSTANT_NODE:
    case NodeTypes.VARBIT_CONSTANT_NODE:
    case NodeTypes.CHAR_CONSTANT_NODE:
    case NodeTypes.DECIMAL_CONSTANT_NODE:
    case NodeTypes.DOUBLE_CONSTANT_NODE:
    case NodeTypes.FLOAT_CONSTANT_NODE:
    case NodeTypes.INT_CONSTANT_NODE:
    case NodeTypes.LONGINT_CONSTANT_NODE:
    case NodeTypes.LONGVARBIT_CONSTANT_NODE:
    case NodeTypes.LONGVARCHAR_CONSTANT_NODE:
    case NodeTypes.SMALLINT_CONSTANT_NODE:
    case NodeTypes.TINYINT_CONSTANT_NODE:
    case NodeTypes.USERTYPE_CONSTANT_NODE:
    case NodeTypes.VARCHAR_CONSTANT_NODE:
    case NodeTypes.BLOB_CONSTANT_NODE:
    case NodeTypes.CLOB_CONSTANT_NODE:
    case NodeTypes.XML_CONSTANT_NODE:
      return constantNode((ConstantNode)node);
    case NodeTypes.PARAMETER_NODE:
      return parameterNode((ParameterNode)node);
    default:
      return "**UNKNOWN(" + node.getNodeType() +")**";
    }
  }

  protected String cursorNode(CursorNode node) throws StandardException {
    String result = toString(node.getResultSetNode());
    if (node.getOrderByList() != null) {
      result += " " + toString(node.getOrderByList());
    }
    return result;
  }

  protected String selectNode(SelectNode node) throws StandardException {
    StringBuilder str = new StringBuilder("SELECT ");
    if (node.isDistinct())
      str.append("DISTINCT ");
    str.append(toString(node.getResultColumns()));
    str.append(" FROM ");
    str.append(toString(node.getFromList()));
    if (node.getWhereClause() != null) {
      str.append(" WHERE ");
      str.append(toString(node.getWhereClause()));
    }
    if (node.getGroupByList() != null) {
      str.append(" ");
      str.append(toString(node.getGroupByList()));
    }
    if (node.getHavingClause() != null) {
      str.append(" HAVING ");
      str.append(toString(node.getHavingClause()));
    }
    return str.toString();
  }

  protected String insertNode(InsertNode node) throws StandardException {
    StringBuilder str = new StringBuilder("INSERT INTO ");
    str.append(toString(node.getTargetTableName()));
    if (node.getTargetColumnList() != null) {
      str.append("(");
      str.append(toString(node.getTargetColumnList()));
      str.append(")");
    }
    str.append(" ");
    str.append(toString(node.getResultSetNode()));
    if (node.getOrderByList() != null) {
      str.append(" ");
      str.append(toString(node.getOrderByList()));
    }
    return str.toString();
  }

  protected String updateNode(UpdateNode unode) throws StandardException {
    // Cf. Parser's getUpdateNode().
    SelectNode snode = (SelectNode)unode.getResultSetNode();
    StringBuilder str = new StringBuilder("UPDATE ");
    str.append(toString(unode.getTargetTableName()));
    str.append(" SET ");
    boolean first = true;
    for (ResultColumn col : snode.getResultColumns()) {
      if (first)
        first = false;
      else
        str.append(", ");
      str.append(toString(col.getReference()));
      str.append(" = ");
      str.append(maybeParens(col.getExpression()));
    }
    if (snode.getWhereClause() != null) {
      str.append(" WHERE ");
      str.append(toString(snode.getWhereClause()));
    }
    return str.toString();
  }

  protected String deleteNode(DeleteNode dnode) throws StandardException {
    // Cf. Parser's getDeleteNode().
    SelectNode snode = (SelectNode)dnode.getResultSetNode();
    StringBuilder str = new StringBuilder("DELETE FROM ");
    str.append(toString(dnode.getTargetTableName()));
    if (snode.getWhereClause() != null) {
      str.append(" WHERE ");
      str.append(toString(snode.getWhereClause()));
    }
    return str.toString();
  }

  protected String rowResultSetNode(RowResultSetNode node) throws StandardException {
    return "VALUES(" + toString(node.getResultColumns()) + ")";
  }

  protected String resultColumnList(ResultColumnList node) throws StandardException {
    return nodeList(node);
  }
  
  protected String resultColumn(ResultColumn node) throws StandardException {
    if (node.getReference() != null)
      return toString(node.getReference());

    String x = toString(node.getExpression());
    String n = node.getName();
    if ((n == null) || n.equals(x))
      return x;
    else
      return x + " AS " + n;
  }

  protected String fromList(FromList node) throws StandardException {
    return nodeList(node);
  }

  protected String fromBaseTable(FromBaseTable node) throws StandardException {
    String tn = toString(node.getTableName());
    String n = node.getCorrelationName();
    if (n == null)
      return tn;
    else
      return tn + " AS " + n;
  }

  protected String tableName(TableName node) throws StandardException {
    return node.getFullTableName();
  }

  protected String columnReference(ColumnReference node) throws StandardException {
    return node.getSQLColumnName();
  }

  protected String groupByList(GroupByList node) throws StandardException {
    return "GROUP BY " + nodeList(node);
  }

  protected String groupByColumn(GroupByColumn node) throws StandardException {
    return toString(node.getColumnExpression());
  }

  protected String orderByList(OrderByList node) throws StandardException {
    return "ORDER BY " + nodeList(node);
  }

  protected String orderByColumn(OrderByColumn node) throws StandardException {
    String result = toString(node.getExpression());
    if (!node.isAscending()) {
      result += " DESC";
    }
    if (node.isNullsOrderedLow()) {
      result += " NULLS FIRST";
    }
    return result;
  }

  protected String binaryLogicalOperatorNode(BinaryLogicalOperatorNode node) 
      throws StandardException {
    return infixBinary(node);
  }

  protected String binaryComparisonOperatorNode(BinaryComparisonOperatorNode node)
    throws StandardException {
    return infixBinary(node);
  }

  protected String binaryArithmeticOperatorNode(BinaryArithmeticOperatorNode node) 
      throws StandardException {
    return infixBinary(node);
  }

  protected String concatenationOperatorNode(ConcatenationOperatorNode node)
      throws StandardException {
    return infixBinary(node);
  }

  protected String notNode(NotNode node) throws StandardException {
    return prefixUnary(node);
  }

  protected String isNullnode(IsNullNode node) throws StandardException {
    return suffixUnary(node);
  }

  protected String aggregateNode(AggregateNode node) throws StandardException {
    return node.getAggregateName() + "(" + toString(node.getOperand()) + ")";
  }

  protected String likeEscapeOperatorNode(LikeEscapeOperatorNode node) 
      throws StandardException {
    return maybeParens(node.getReceiver()) +
      " " + node.getOperator().toUpperCase() + " " +
      maybeParens(node.getLeftOperand());
  }

  protected String inListOperatorNode(InListOperatorNode node) throws StandardException {
    return maybeParens(node.getLeftOperand()) +
      " IN (" + toString(node.getRightOperandList()) + ")";
  }

  protected String valueNodeList(ValueNodeList node) throws StandardException {
    return nodeList(node);
  }

  protected String betweenOperatorNode(BetweenOperatorNode node)
      throws StandardException {
    return maybeParens(node.getLeftOperand()) +
      " BETWEEN " + maybeParens(node.getRightOperandList().get(0)) +
      " AND " + maybeParens(node.getRightOperandList().get(1));
  }

  protected String constantNode(ConstantNode node) throws StandardException {
    Object value = node.getValue();
    if (value == null)
      return "NULL";
    else if (value instanceof String)
      return "'" + ((String)value).replace("'", "''") + "'";
    else if (value instanceof byte[])
      return hexConstant((byte[])value);
    else
      return value.toString();
  }

  protected String prefixUnary(UnaryOperatorNode node) throws StandardException {
    return node.getOperator().toUpperCase() + " " +
      maybeParens(node.getOperand());
  }

  protected String suffixUnary(UnaryOperatorNode node) throws StandardException {
    return maybeParens(node.getOperand()) + " " +
      node.getOperator().toUpperCase();
  }

  protected String infixBinary(BinaryOperatorNode node) throws StandardException {
    return maybeParens(node.getLeftOperand()) +
      " " + node.getOperator().toUpperCase() + " " +
      maybeParens(node.getRightOperand());
  }
  
  protected String nodeList(QueryTreeNodeList<? extends QueryTreeNode> nl)
      throws StandardException {
    StringBuilder str = new StringBuilder();
    boolean first = true;
    for (QueryTreeNode node : nl) {
      if (first)
        first = false;
      else
        str.append(", ");
      str.append(maybeParens(node));
    }
    return str.toString();
  }

  protected String maybeParens(QueryTreeNode node) throws StandardException {
    String str = toString(node);
    if (node instanceof ConstantNode)
      return str;
    else if (str.indexOf(' ') < 0)
      return str;
    else
      return "(" + str + ")";
  }

  protected String hexConstant(byte[] value) {
    StringBuilder str = new StringBuilder("X'");
    for (byte b : value) {
      str.append(Integer.toString((int)b & 0xFF, 16).toUpperCase());
    }
    str.append("'");
    return str.toString();
  }

  protected String parameterNode(ParameterNode node) throws StandardException {
    return "?";
  }

  // TODO: Temporary low-budget testing.
  public static void main(String[] args) throws Exception {
    SQLParser p = new SQLParser();
    NodeToString ts = new NodeToString();
    for (String arg : args) {
      System.out.println("=====");
      System.out.println(arg);
      try {
        StatementNode stmt = p.parseStatement(arg);
        String sql = ts.toString(stmt);
        System.out.println(sql);
        if (sql.indexOf("UNKNOWN") > 0) {
          stmt.treePrint();
        }
      }
      catch (StandardException ex) {
        ex.printStackTrace();
      }
    }
  }

}
