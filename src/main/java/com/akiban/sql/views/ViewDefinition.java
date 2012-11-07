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

package com.akiban.sql.views;

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;

public class ViewDefinition
{
    private CreateViewNode definition;
    private FromSubquery subquery;

    /**
     * Parse the given SQL as CREATE VIEW and remember the definition.
     */
    public ViewDefinition(String sql, SQLParser parser)
            throws StandardException {
        this(parser.parseStatement(sql), parser);
    }

    public ViewDefinition(StatementNode parsed, SQLParserContext parserContext)
            throws StandardException {
        if (parsed.getNodeType() != NodeTypes.CREATE_VIEW_NODE) {
            throw new StandardException("Parsed statement was not a view");
        }
        definition = (CreateViewNode)parsed;
        subquery = (FromSubquery)
            parserContext.getNodeFactory().getNode(NodeTypes.FROM_SUBQUERY,
                                                   definition.getParsedQueryExpression(),
                                                   definition.getOrderByList(),
                                                   definition.getOffset(),
                                                   definition.getFetchFirst(),
                                                   getName().getTableName(),
                                                   definition.getResultColumns(),
                                                   null,
                                                   parserContext);
    }

    /** 
     * Get the name of the view.
     */
    public TableName getName() {
        return definition.getObjectName();
    }

    /**
     * Get the text of the view definition.
     */
    public String getQueryExpression() {
        return definition.getQueryExpression();
    }

    /**
     * Get the result columns for this view.
     */
    public ResultColumnList getResultColumns() {
        ResultColumnList rcl = subquery.getResultColumns();
        if (rcl == null)
            rcl = subquery.getSubquery().getResultColumns();
        return rcl;
    }

    /**
     * Get the original subquery for binding.
     */
    public FromSubquery getSubquery() {
        return subquery;
    }

    /**
     * Get the view as an equivalent subquery belonging to the given context.
     */
    public FromSubquery copySubquery(SQLParserContext parserContext) 
            throws StandardException {
        return (FromSubquery)
            parserContext.getNodeFactory().copyNode(subquery, parserContext);
    }

    /**
     * @deprecated
     * @see #copySubquery
     */
    @Deprecated
    public FromSubquery getSubquery(Visitor binder) throws StandardException {
        subquery = (FromSubquery)subquery.accept(binder);
        return copySubquery(subquery.getParserContext());
    }

}
