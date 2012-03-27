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

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

/**
 * An IndexColumn is the element of an index definition.
 */
public class IndexColumn extends QueryTreeNode
{
    private TableName tableName;
    private String columnName;
    private boolean ascending = true;

    /**
     * Initializer.
     *
     * @param columnName Name of the column
     * @param ascending Whether index is ascending
     */
    public void init(Object columnName,
                     Object ascending) {
        this.tableName = null;
        this.columnName = (String)columnName;
        this.ascending = ((Boolean)ascending).booleanValue();
    }

    /**
     * Initializer.
     *
     * @param tableName Table holding indexed column
     * @param columnName Name of the column
     * @param ascending Whether index is ascending
     */
    public void init(Object tableName,
                     Object columnName,
                     Object ascending) {
        this.tableName = (TableName)tableName;
        this.columnName = (String)columnName;
        this.ascending = ((Boolean)ascending).booleanValue();
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        IndexColumn other = (IndexColumn)node;
        this.tableName = (TableName)getNodeFactory().copyNode(other.tableName, 
                                                              getParserContext());
        this.columnName = other.columnName;
        this.ascending = other.ascending;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */
    public String toString() {
        return "columnName: " + columnName + "\n" +
            "tableName: " + ((tableName != null) ? tableName.toString() : "null") + "\n" +
            (ascending ? "ascending" : "descending") + "\n" +
            super.toString();
    }

    public TableName getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    /**
     * @return true if ascending, false if descending
     */
    public boolean isAscending() {
        return ascending;
    }

}
