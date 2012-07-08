/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
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
                                                   null, null, null,
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
     * Get the result columns for this view.
     * Also binds the view to detect unresolved references.
     */
    public ResultColumnList getResultColumns() {
        return definition.getResultColumns();
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
