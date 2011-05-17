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
