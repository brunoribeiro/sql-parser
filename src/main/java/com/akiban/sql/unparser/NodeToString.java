/**
 * Copyright © 2012 Akiban Technologies, Inc.  All rights
 * reserved.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact
 * licensing@akiban.com.
 *
 * Contributors:
 * Akiban Technologies, Inc.
 */

package com.akiban.sql.unparser;

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;

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
        case NodeTypes.DROP_TABLE_NODE:
        case NodeTypes.DROP_VIEW_NODE:
        case NodeTypes.DROP_TRIGGER_NODE:
            return qualifiedDDLNode((DDLStatementNode)node);
        case NodeTypes.DROP_INDEX_NODE:
            return dropIndexNode((DropIndexNode)node);
        case NodeTypes.EXPLAIN_STATEMENT_NODE:
            return explainStatementNode((ExplainStatementNode)node);
        case NodeTypes.TRANSACTION_CONTROL_NODE:
            return transactionControlNode((TransactionControlNode)node);
        case NodeTypes.SET_TRANSACTION_ISOLATION_NODE:
            return setTransactionIsolationNode((SetTransactionIsolationNode)node);
        case NodeTypes.SET_TRANSACTION_ACCESS_NODE:
            return setTransactionAccessNode((SetTransactionAccessNode)node);
        case NodeTypes.SET_CONFIGURATION_NODE:
            return setConfigurationNode((SetConfigurationNode)node);
        case NodeTypes.TABLE_ELEMENT_LIST:
            return tableElementList((TableElementList)node);
        case NodeTypes.COLUMN_DEFINITION_NODE:
            return columnDefinitionNode((ColumnDefinitionNode)node);
        case NodeTypes.CONSTRAINT_DEFINITION_NODE:
            return constraintDefinitionNode((ConstraintDefinitionNode)node);
        case NodeTypes.FK_CONSTRAINT_DEFINITION_NODE:
            return fkConstraintDefinitionNode((FKConstraintDefinitionNode)node);
        case NodeTypes.CREATE_INDEX_NODE:
            return createIndexNode((CreateIndexNode)node);
        case NodeTypes.INDEX_COLUMN_LIST:
            return indexColumnList((IndexColumnList)node);
        case NodeTypes.INDEX_COLUMN:
            return indexColumn((IndexColumn)node);
        case NodeTypes.CREATE_ALIAS_NODE:
            return createAliasNode((CreateAliasNode)node);
        case NodeTypes.RENAME_NODE:
            return renameNode((RenameNode)node);
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
        case NodeTypes.UNION_NODE:
            return unionNode((UnionNode)node);
        case NodeTypes.GROUP_BY_LIST:
            return groupByList((GroupByList)node);
        case NodeTypes.GROUP_CONCAT_NODE:
            return groupConcat((GroupConcatNode)node);
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
        case NodeTypes.VIRTUAL_COLUMN_NODE:
            return virtualColumnNode((VirtualColumnNode)node);
        case NodeTypes.ROW_RESULT_SET_NODE:
            return rowResultSetNode((RowResultSetNode)node);
        case NodeTypes.ROWS_RESULT_SET_NODE:
            return rowsResultSetNode((RowsResultSetNode)node);
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
        case NodeTypes.BINARY_DIV_OPERATOR_NODE:
        case NodeTypes.BINARY_MINUS_OPERATOR_NODE:
            return binaryArithmeticOperatorNode((BinaryArithmeticOperatorNode)node);
        case NodeTypes.CONCATENATION_OPERATOR_NODE:
            return concatenationOperatorNode((ConcatenationOperatorNode)node);
        case NodeTypes.NOT_NODE:
            return notNode((NotNode)node);
        case NodeTypes.IS_NULL_NODE:
        case NodeTypes.IS_NOT_NULL_NODE:
            return isNullNode((IsNullNode)node);
        case NodeTypes.IS_NODE:
            return isNode((IsNode)node);
        case NodeTypes.UNARY_DATE_TIMESTAMP_OPERATOR_NODE:
          return unaryDateTimestampOperatorNode((UnaryDateTimestampOperatorNode)node);
        case NodeTypes.LIKE_OPERATOR_NODE:
            return likeEscapeOperatorNode((LikeEscapeOperatorNode)node);
        case NodeTypes.IN_LIST_OPERATOR_NODE:
            return inListOperatorNode((InListOperatorNode)node);
        case NodeTypes.ROW_CTOR_NODE:
            return rowCtorNode((RowConstructorNode)node);
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
        case NodeTypes.USER_NODE:
            return "USER";
        case NodeTypes.CURRENT_USER_NODE:
            return "CURRENT_USER";
        case NodeTypes.SESSION_USER_NODE:
            return "SESSION_USER";
        case NodeTypes.SYSTEM_USER_NODE:
            return "SYSTEM_USER";
        case NodeTypes.CURRENT_ISOLATION_NODE:
            return "CURRENT ISOLATION";
        case NodeTypes.IDENTITY_VAL_NODE:
            return "IDENTITY_VAL_LOCAL()";
        case NodeTypes.CURRENT_SCHEMA_NODE:
            return "CURRENT SCHEMA";
        case NodeTypes.CURRENT_ROLE_NODE:
            return "CURRENT_ROLE";
        case NodeTypes.CURRENT_DATETIME_OPERATOR_NODE:
            return currentDatetimeOperatorNode((CurrentDatetimeOperatorNode)node);
        case NodeTypes.CAST_NODE:
            return castNode((CastNode)node);
        case NodeTypes.JAVA_TO_SQL_VALUE_NODE:
            return javaToSQLValueNode((JavaToSQLValueNode)node);
        case NodeTypes.SQL_TO_JAVA_VALUE_NODE:
            return sqlToJavaValueNode((SQLToJavaValueNode)node);
        case NodeTypes.STATIC_METHOD_CALL_NODE:
            return staticMethodCallNode((StaticMethodCallNode)node);
        case NodeTypes.CALL_STATEMENT_NODE:
            return callStatementNode((CallStatementNode)node);
        case NodeTypes.INDEX_CONSTRAINT_NODE:
            return indexConstraint((IndexConstraintDefinitionNode)node);
        case NodeTypes.DECLARE_STATEMENT_NODE:
            return declareStatementNode((DeclareStatementNode)node);
        case NodeTypes.FETCH_STATEMENT_NODE:
            return fetchStatementNode((FetchStatementNode)node);
        case NodeTypes.CLOSE_STATEMENT_NODE:
            return closeStatementNode((CloseStatementNode)node);
        case NodeTypes.PREPARE_STATEMENT_NODE:
            return prepareStatementNode((PrepareStatementNode)node);
        case NodeTypes.EXECUTE_STATEMENT_NODE:
            return executeStatementNode((ExecuteStatementNode)node);
        case NodeTypes.DEALLOCATE_STATEMENT_NODE:
            return deallocateStatementNode((DeallocateStatementNode)node);
        default:
            return "**UNKNOWN(" + node.getNodeType() +")**";
        }
    }

    protected String indexConstraint(IndexConstraintDefinitionNode node) throws StandardException
    {
        StringBuilder builder = new StringBuilder("INDEX ");
        
        String indexName = node.getIndexName();
        
        if (indexName != null)
            builder.append(indexName).append(' ');
        
        builder.append('(')
               .append(indexColumnList(node.getIndexColumnList()))
               .append(')');
        
        StorageLocation loc = node.getLocation();
        if (loc != null)
            builder.append(" AS ").append(loc);
        
        return builder.toString();
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

    protected String fkConstraintDefinitionNode(FKConstraintDefinitionNode node)
            throws StandardException {
        StringBuilder str = new StringBuilder();
        if (node.isGrouping())
            str.append("GROUPING ");
        str.append("FOREIGN KEY(");
        str.append(toString(node.getColumnList()));
        str.append(") REFERENCES ");
        str.append(toString(node.getRefTableName()));
        str.append("(");
        str.append(toString(node.getColumnList()));
        str.append(")");
        return str.toString();
    }

    protected String createIndexNode(CreateIndexNode node) throws StandardException {
        StringBuilder str = new StringBuilder("CREATE ");
        if (node.getUniqueness())
            str.append("UNIQUE ");
        str.append("INDEX");
        str.append(" ");
        
        switch (node.getExistenceCheck())
        {
            case IF_EXISTS:
                str.append("IF EXISTS ");
                break;
            case IF_NOT_EXISTS:
                str.append("IF NOT EXISTS ");
                break;  
        }

        str.append(toString(node.getIndexName()));
        str.append(" ON ");
        str.append(node.getIndexTableName());
        str.append("(");
        str.append(toString(node.getColumnList()));
        str.append(")");
        if (node.getJoinType() != null) {
            str.append(String.format(" USING %s JOIN",
                                     node.getJoinType() == JoinNode.JoinType.LEFT_OUTER ? "LEFT" : "RIGHT"));
        }
        return str.toString();
    }

    protected String indexColumnList(IndexColumnList node) throws StandardException {

        StringBuilder buffer = new StringBuilder();
        int firstFunctionArg = node.firstFunctionArg();
        int lastFunctionArg = node.lastFunctionArg();
        int arg = 0;
        while (arg < node.size()) {
            if (arg > 0) {
                buffer.append(", ");
            }
            if (arg == firstFunctionArg) {
                buffer.append(node.functionType());
                buffer.append('(');
            }
            buffer.append(toString(node.get(arg)));
            if (arg == lastFunctionArg) {
                buffer.append(')');
            }
            arg++;
        }
        return buffer.toString();
    }

    protected String indexColumn(IndexColumn node) throws StandardException {
        StringBuilder str = new StringBuilder();
        if (node.getTableName() != null) {
            str.append(toString(node.getTableName()));
            str.append(".");
        }
        str.append(node.getColumnName());
        if (!node.isAscending())
            str.append(" DESC");
        return str.toString();
    }

    protected String createAliasNode(CreateAliasNode node) throws StandardException {
        StringBuilder str = new StringBuilder(node.statementToString());
        str.append(' ');
        str.append(toString(node.getObjectName()));
        switch (node.getAliasType()) {
        case PROCEDURE:
        case FUNCTION:
            str.append(node.getAliasInfo());
            if (node.getDefinition() != null) {
                str.append(" AS '");
                if (node.getDefinition().indexOf('\n') >= 0) {
                    str.append("$$");
                    str.append(node.getDefinition());
                    str.append("$$");
                }
                else {
                    str.append(node.getDefinition().replace("'", "''"));
                }
                str.append('\'');
            }
            else {
                str.append(" EXTERNAL NAME '");
                str.append(node.getJavaClassName());
                if (node.getMethodName() != null) {
                    str.append('.');
                    str.append(node.getMethodName());
                }
                str.append('\'');
            }
            break;
        }
        return str.toString();
    }

    protected String renameNode(RenameNode node) throws StandardException {
        if (node.isAlterTable()) {
            return "ALTER TABLE " + toString(node.getObjectName()) +
                "RENAME COLUMN " + node.getOldObjectName() +
                " TO " + node.getNewObjectName();
        }
        else if (node.getRenameType() == RenameNode.RenameType.INDEX) {
            if (node.getObjectName() == null) {
                return node.statementToString() + " " + node.getOldObjectName() +
                    " TO " + node.getNewObjectName();
            }
            else {
                return node.statementToString() + " " + toString(node.getObjectName()) +
                    "." + node.getOldObjectName() +
                    " TO " + node.getNewObjectName();
            }
        }
        else {
            return node.statementToString() + " " + toString(node.getObjectName()) +
                " TO " + toString(node.getNewTableName());
        }
    }

    protected String dropIndexNode(DropIndexNode node) throws StandardException {
        StringBuilder str = new StringBuilder(node.statementToString());
        str.append(" ");
        if (node.getObjectName() != null) {
            str.append(toString(node.getObjectName()));
            str.append(".");
        }
        str.append(node.getIndexName());
        return str.toString();
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
        if (!node.getFromList().isEmpty()) {
            str.append(" FROM ");
            str.append(toString(node.getFromList()));
        }
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
        if (node.getReturningList() != null) {
            str.append(" RETURNING ");
            str.append(toString(node.getReturningList()));
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
        if (unode.getReturningList() != null) {
            str.append(" RETURNING ");
            str.append(toString(unode.getReturningList()));
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
        if (dnode.getReturningList() != null) {
            str.append(" RETURNING ");
            str.append(toString(dnode.getReturningList()));
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

    protected String rowsResultSetNode(RowsResultSetNode node) throws StandardException {
        StringBuilder str = new StringBuilder("VALUES");
        boolean first = true;
        for (RowResultSetNode row : node.getRows()) {
            if (first)
                first = false;
            else
                str.append(", ");
            str.append("(");
            str.append(toString(row.getResultColumns()));
            str.append(")");
        }
        return str.toString();
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
        if (node.isNaturalJoin())
            str.append("NATURAL ");
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

    protected String unionNode(UnionNode node) throws StandardException {
        return toString(node.getLeftResultSet()) + " UNION " + 
               toString(node.getRightResultSet());
    }

    protected String tableName(TableName node) throws StandardException {
        return node.getFullTableName();
    }

    protected String columnReference(ColumnReference node) throws StandardException {
        return node.getSQLColumnName();
    }

    protected String virtualColumnNode(VirtualColumnNode node) throws StandardException {
        return node.getSourceColumn().getName();
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

    protected String isNullNode(IsNullNode node) throws StandardException {
        return suffixUnary(node);
    }

    protected String unaryDateTimestampOperatorNode(UnaryDateTimestampOperatorNode node) 
            throws StandardException {
        return functionUnary(node);
    }

    protected String isNode(IsNode node) throws StandardException {
        StringBuilder str = new StringBuilder(maybeParens(node.getLeftOperand()));
        str.append(" IS ");
        if (node.isNegated())
            str.append("NOT ");
        ValueNode rightOperand = node.getRightOperand();
        if (rightOperand instanceof BooleanConstantNode) {
            Boolean value = (Boolean)((BooleanConstantNode)rightOperand).getValue();
            if (value == null)
                str.append("UNKNOWN");
            else
                str.append(value.toString().toUpperCase());
        }
        else
            str.append(maybeParens(rightOperand));
        return str.toString();
    }

    protected String aggregateNode(AggregateNode node) throws StandardException {
        if (node.getOperand() == null)
            return node.getAggregateName();
        else
            return node.getAggregateName() + "(" + toString(node.getOperand()) + ")";
    }

    protected String likeEscapeOperatorNode(LikeEscapeOperatorNode node) 
            throws StandardException {
        String like = maybeParens(node.getReceiver()) +
            " " + node.getOperator().toUpperCase() + " " +
            maybeParens(node.getLeftOperand());
        if (node.getRightOperand() != null)
            like += " ESCAPE " + maybeParens(node.getRightOperand());
        return like;
    }

    protected String inListOperatorNode(InListOperatorNode node) throws StandardException {
        return maybeParens(node.getLeftOperand()) +
            " " + (node.isNegated() ? "NOT IN" : "IN") + 
            " (" + toString(node.getRightOperandList()) + ")";
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
        else if (value instanceof Double)
            return String.format("%e", value);
        else if (value instanceof Boolean)
            return value.toString().toUpperCase();
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

    protected String functionUnary(UnaryOperatorNode node) throws StandardException {
        return node.getOperator().toUpperCase() + "(" +
            toString(node.getOperand()) + ")";
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

    protected String currentDatetimeOperatorNode(CurrentDatetimeOperatorNode node) 
            throws StandardException {
        switch (node.getField()) {
        case DATE:
            return "CURRENT_DATE";
        case TIME:
            return "CURRENT_TIME";
        case TIMESTAMP:
            return "CURRENT_TIMESTAMP";
        default:
            return "**UNKNOWN(" + node.getField() +")**";
        }
    }

    protected String castNode(CastNode node) throws StandardException {
        return "CAST(" + toString(node.getCastOperand()) + 
            " AS " + node.getType().toString() + ")";
    }

    protected String javaToSQLValueNode(JavaToSQLValueNode node) 
            throws StandardException {
        return toString(node.getJavaValueNode());
    }

    protected String sqlToJavaValueNode(SQLToJavaValueNode node)
            throws StandardException {
        return toString(node.getSQLValueNode());
    }

    protected String staticMethodCallNode(StaticMethodCallNode node)
            throws StandardException {
        StringBuilder str = new StringBuilder();
        if (node.getProcedureName() != null)
            str.append(toString(node.getProcedureName()));
        else
            str.append(node.getMethodName());
        str.append("(");
        JavaValueNode[] params = node.getMethodParameters();
        for (int i = 0; i < params.length; i++) {
            if (i > 0) str.append(", ");
            str.append(maybeParens(params[i]));
        }
        str.append(")");
        return str.toString();
    }

    protected String callStatementNode(CallStatementNode node) throws StandardException {
        return "CALL " + javaToSQLValueNode(node.methodCall());
    }

    protected String qualifiedDDLNode(DDLStatementNode node) throws StandardException {
        return node.statementToString() + " " + node.getObjectName();
    }

    protected String explainStatementNode(ExplainStatementNode node) 
            throws StandardException {
        return "EXPLAIN " + toString(node.getStatement());
    }

    protected String transactionControlNode(TransactionControlNode node)
            throws StandardException {
        return node.statementToString();
    }
    
    protected String setTransactionIsolationNode(SetTransactionIsolationNode node)
            throws StandardException {
        return node.statementToString() + " " + node.getIsolationLevel().getSyntax();
    }
    
    protected String setTransactionAccessNode(SetTransactionAccessNode node)
            throws StandardException {
        return node.statementToString() + " " + node.getAccessMode().getSyntax();
    }

    protected String setConfigurationNode(SetConfigurationNode node)
            throws StandardException {
        return node.statementToString() + " = '" + node.getValue() + "'";
    }

    protected String rowCtorNode(RowConstructorNode row) throws StandardException
    {
        ValueNodeList list = row.getNodeList();
        
        switch(list.size())
        {
            case 0:
                return "EMPTY";
            case 1:
                QueryTreeNode node = list.get(0);
                if (!(node instanceof RowConstructorNode))
                    return toString(node);
        }
        
        StringBuilder bd = new StringBuilder();
        for (QueryTreeNode node : list )
        {
            doPrint(node, bd);
            bd.append(", ");
        }

        return bd.substring(0, bd.length() -2); // delete the last (<COMMA> <SPACE>)
    }
    
    protected String declareStatementNode(DeclareStatementNode node) 
            throws StandardException {
        return "DECLARE " + node.getName() + " CURSOR FOR " +
            toString(node.getStatement());
    }

    protected String fetchStatementNode(FetchStatementNode node)
            throws StandardException {
        return "FETCH " + 
            ((node.getCount() < 0) ? "ALL" : Integer.toString(node.getCount())) +
            " FROM " + node.getName();
    }

    protected String closeStatementNode(CloseStatementNode node)
            throws StandardException {
        return "CLOSE " + node.getName();
    }

    protected String prepareStatementNode(PrepareStatementNode node)
            throws StandardException {
        return "PREPARE " + node.getName() + " AS " +
            toString(node.getStatement());
    }

    protected String executeStatementNode(ExecuteStatementNode node)
            throws StandardException {
        return "EXECUTE " + node.getName() + 
            "(" + nodeList(node.getParameterList(), true) + ")";
    }

    protected String deallocateStatementNode(DeallocateStatementNode node)
            throws StandardException {
        return "DEALLOCATE " + node.getName();
    }
     
    protected void doPrint(QueryTreeNode node, StringBuilder bd) throws StandardException
    {
        if (node instanceof RowConstructorNode)
            bd.append(rowCtorNode((RowConstructorNode)node));
        else
            bd.append(toString(node));
    }
    
    protected String groupConcat(GroupConcatNode node) throws StandardException
    {
        StringBuilder ret = new StringBuilder("GROUP_CONCAT(");
        
        ret.append(node.getOperand());
        
        OrderByList orderBy = node.getOrderBy();
        if (orderBy != null)
            ret.append(this.toString(orderBy));
        
        // i
        ret.append("SEPARATOR \'").append(node.getSeparator()).append("\')");
        return ret.toString();
    }
}
