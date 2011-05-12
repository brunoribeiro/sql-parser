/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
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
        case NodeTypes.CREATE_TABLE_NODE:
            return createTableNode((CreateTableNode)node);
        case NodeTypes.CREATE_VIEW_NODE:
            return createViewNode((CreateViewNode)node);
        case NodeTypes.CREATE_GROUP_NODE:
        case NodeTypes.DROP_TABLE_NODE:
        case NodeTypes.DROP_INDEX_NODE:
        case NodeTypes.DROP_VIEW_NODE:
        case NodeTypes.DROP_TRIGGER_NODE:
        case NodeTypes.DROP_GROUP_NODE:
        case NodeTypes.TRUNCATE_GROUP_NODE:
            return qualifiedDDLNode((DDLStatementNode)node);
        case NodeTypes.TABLE_ELEMENT_LIST:
            return tableElementList((TableElementList)node);
        case NodeTypes.COLUMN_DEFINITION_NODE:
            return columnDefinitionNode((ColumnDefinitionNode)node);
        case NodeTypes.CONSTRAINT_DEFINITION_NODE:
            return constraintDefinitionNode((ConstraintDefinitionNode)node);
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
        case NodeTypes.SUBQUERY_NODE:
            return subqueryNode((SubqueryNode)node);
        case NodeTypes.RESULT_COLUMN_LIST:
            return resultColumnList((ResultColumnList)node);
        case NodeTypes.RESULT_COLUMN:
            return resultColumn((ResultColumn)node);
        case NodeTypes.ALL_RESULT_COLUMN:
            return allResultColumn((AllResultColumn)node);
        case NodeTypes.FROM_LIST:
            return fromList((FromList)node);
        case NodeTypes.JOIN_NODE:
        case NodeTypes.HALF_OUTER_JOIN_NODE:
            return joinNode((JoinNode)node);
        case NodeTypes.GROUP_BY_LIST:
            return groupByList((GroupByList)node);
        case NodeTypes.ORDER_BY_LIST:
            return orderByList((OrderByList)node);
        case NodeTypes.VALUE_NODE_LIST:
            return valueNodeList((ValueNodeList)node);
        case NodeTypes.FROM_BASE_TABLE:
            return fromBaseTable((FromBaseTable)node);
        case NodeTypes.FROM_SUBQUERY:
            return fromSubquery((FromSubquery)node);
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
        case NodeTypes.CONDITIONAL_NODE:
            return conditionalNode((ConditionalNode)node);
        case NodeTypes.COALESCE_FUNCTION_NODE:
            return coalesceFunctionNode((CoalesceFunctionNode)node);
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
        case NodeTypes.CAST_NODE:
            return castNode((CastNode)node);
        default:
            return "**UNKNOWN(" + node.getNodeType() +")**";
        }
    }

    protected String createTableNode(CreateTableNode node) throws StandardException {
        StringBuilder str = new StringBuilder("CREATE TABLE ");
        str.append(toString(node.getObjectName()));
        if (node.getTableElementList() != null) {
            str.append("(");
            str.append(toString(node.getTableElementList()));
            str.append(")");
        }
        if (node.getQueryExpression() != null) {
            str.append(" AS (");
            str.append(toString(node.getQueryExpression()));
            str.append(") WITH ");
            if (!node.isWithData()) str.append("NO ");
            str.append("DATA");
        }
        return str.toString();
    }

    protected String createViewNode(CreateViewNode node) throws StandardException {
        StringBuilder str = new StringBuilder("CREATE VIEW ");
        str.append(toString(node.getObjectName()));
        if (node.getResultColumns() != null) {
            str.append("(");
            str.append(toString(node.getResultColumns()));
            str.append(")");
        }
        str.append(" AS (");
        str.append(toString(node.getParsedQueryExpression()));
        str.append(")");
        return str.toString();
    }

    protected String tableElementList(TableElementList node) throws StandardException {
        return nodeList(node);
    }

    protected String columnDefinitionNode(ColumnDefinitionNode node)
            throws StandardException {
        return node.getColumnName() + " " + node.getType();
    }

    protected String constraintDefinitionNode(ConstraintDefinitionNode node) 
            throws StandardException {
        switch (node.getConstraintType()) {
        case PRIMARY_KEY:
            return "PRIMARY KEY(" + toString(node.getColumnList()) + ")";
        case UNIQUE:
            return "UNIQUE(" + toString(node.getColumnList()) + ")";
        default:
            return "**UNKNOWN(" + node.getConstraintType() + ")";
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
        str.append(toString(snode.getFromList().get(0)));
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
        str.append(toString(snode.getFromList().get(0)));
        if (snode.getWhereClause() != null) {
            str.append(" WHERE ");
            str.append(toString(snode.getWhereClause()));
        }
        return str.toString();
    }

    protected String subqueryNode(SubqueryNode node) throws StandardException {
        String str = toString(node.getResultSet());
        if (node.getOrderByList() != null) {
            str = str + " " + toString(node.getOrderByList());
        }
        str = "(" + str + ")";
        switch (node.getSubqueryType()) {
        case FROM:
        case EXPRESSION:
        default:
            return str;
        case EXISTS:
            return "EXISTS " + str;
        case NOT_EXISTS:
            return "NOT EXISTS " + str;
        case IN:
            return maybeParens(node.getLeftOperand()) + " IN " + str;
        case NOT_IN:
            return maybeParens(node.getLeftOperand()) + " NOT IN " + str;
        case EQ_ANY:
            return maybeParens(node.getLeftOperand()) + " = ANY " + str;
        case EQ_ALL:
            return maybeParens(node.getLeftOperand()) + " = ALL " + str;
        case NE_ANY:
            return maybeParens(node.getLeftOperand()) + " <> ANY " + str;
        case NE_ALL:
            return maybeParens(node.getLeftOperand()) + " <> ALL " + str;
        case GT_ANY:
            return maybeParens(node.getLeftOperand()) + " > ANY " + str;
        case GT_ALL:
            return maybeParens(node.getLeftOperand()) + " > ALL " + str;
        case GE_ANY:
            return maybeParens(node.getLeftOperand()) + " >= ANY " + str;
        case GE_ALL:
            return maybeParens(node.getLeftOperand()) + " > ANY " + str;
        case LT_ANY:
            return maybeParens(node.getLeftOperand()) + " < ANY " + str;
        case LT_ALL:
            return maybeParens(node.getLeftOperand()) + " < ALL " + str;
        case LE_ANY:
            return maybeParens(node.getLeftOperand()) + " <= ANY " + str;
        case LE_ALL:
            return maybeParens(node.getLeftOperand()) + " <= ALL " + str;
        }
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

        String n = node.getName();
        if (node.getExpression() == null)
            return n;

        String x = maybeParens(node.getExpression());
        if ((n == null) || n.equals(x))
            return x;
        else
            return x + " AS " + n;
    }

    protected String allResultColumn(AllResultColumn node) throws StandardException {
        return "*";
    }

    protected String fromList(FromList node) throws StandardException {
        return nodeList(node);
    }

    protected String fromBaseTable(FromBaseTable node) throws StandardException {
        String tn = toString(node.getOrigTableName());
        String n = node.getCorrelationName();
        if (n == null)
            return tn;
        else
            return tn + " AS " + n;
    }

    protected String fromSubquery(FromSubquery node) throws StandardException {
        StringBuilder str = new StringBuilder(toString(node.getSubquery()));
        if (node.getOrderByList() != null) {
            str.append(' ');
            str.append(toString(node.getOrderByList()));
        }
        str.insert(0, '(');
        str.append(')');
        str.append(" AS ");
        str.append(node.getCorrelationName());
        if (node.getResultColumns() != null) {
            str.append('(');
            str.append(toString(node.getResultColumns()));
            str.append(')');
        }
        return str.toString();
    }

    protected String joinNode(JoinNode node) throws StandardException {
        StringBuilder str = new StringBuilder(toString(node.getLeftResultSet()));
        JoinNode.JoinType joinType = JoinNode.JoinType.INNER;
        if (node instanceof HalfOuterJoinNode)
            joinType = ((HalfOuterJoinNode)node).isRightOuterJoin() ? 
                JoinNode.JoinType.RIGHT_OUTER : JoinNode.JoinType.LEFT_OUTER;
        str.append(' ');
        str.append(JoinNode.joinTypeToString(joinType));
        str.append(' ');
        str.append(toString(node.getRightResultSet()));
        if (node.getJoinClause() != null) {
            str.append(" ON ");
            str.append(maybeParens(node.getJoinClause()));
        }
        if (node.getUsingClause() != null) {
            str.append(" USING (");
            str.append(toString(node.getUsingClause()));
            str.append(')');
        }
        return str.toString();
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
        return maybeParens(node.getColumnExpression());
    }

    protected String orderByList(OrderByList node) throws StandardException {
        return "ORDER BY " + nodeList(node);
    }

    protected String orderByColumn(OrderByColumn node) throws StandardException {
        String result = maybeParens(node.getExpression());
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
        return nodeList(node, true);
    }

    protected String betweenOperatorNode(BetweenOperatorNode node)
            throws StandardException {
        return maybeParens(node.getLeftOperand()) +
            " BETWEEN " + maybeParens(node.getRightOperandList().get(0)) +
            " AND " + maybeParens(node.getRightOperandList().get(1));
    }

    protected String conditionalNode(ConditionalNode node) throws StandardException {
        StringBuilder str = new StringBuilder("CASE");
        while (true) {
            str.append(" WHEN ");
            str.append(maybeParens(node.getTestCondition()));
            str.append(" THEN ");
            str.append(maybeParens(node.getThenNode()));
            ValueNode elseNode = node.getElseNode();
            if (elseNode instanceof ConditionalNode)
                node = (ConditionalNode)elseNode;
            else {
                str.append(" ELSE ");
                str.append(maybeParens(elseNode));
                break;
            }
        }
        str.append(" END");
        return str.toString();
    }

    protected String coalesceFunctionNode(CoalesceFunctionNode node) 
            throws StandardException {
        return functionCall(node.getFunctionName(),
                                                node.getArgumentsList());
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
    
    protected String functionCall(String functionName, ValueNodeList args)
            throws StandardException {
        return functionName + "(" + nodeList(args, true) + ")";
    }

    protected String nodeList(QueryTreeNodeList<? extends QueryTreeNode> nl)
            throws StandardException {
        return nodeList(nl, false);
    }

    protected String nodeList(QueryTreeNodeList<? extends QueryTreeNode> nl, boolean expr)
            throws StandardException {
        StringBuilder str = new StringBuilder();
        boolean first = true;
        for (QueryTreeNode node : nl) {
            if (first)
                first = false;
            else
                str.append(", ");
            str.append(expr ? maybeParens(node) : toString(node));
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
        return "$" + (node.getParameterNumber() + 1);
    }

    protected String castNode(CastNode node) throws StandardException {
        return "CAST(" + toString(node.getCastOperand()) + 
            " AS " + node.getType().toString() + ")";
    }

    protected String qualifiedDDLNode(DDLStatementNode node) throws StandardException {
        return node.statementToString() + " " + node.getObjectName();
    }

}
