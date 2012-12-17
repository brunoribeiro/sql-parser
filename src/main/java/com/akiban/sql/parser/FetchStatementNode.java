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

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

/**
 * FETCH rows from declared cursor.
 */

public class FetchStatementNode extends StatementNode
{
    private String name;
    private int count;

    /**
     * Initializer for an FetchStatementNode
     *
     * @param name The name of the cursor
     * @param count The number of rows to fetch
     */

    public void init(Object name,
                     Object count) {
        this.name = (String)name;
        this.count = (Integer)count;
    }
    
    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);
        
        FetchStatementNode other = (FetchStatementNode)node;
        this.name = other.name;
        this.count = other.count;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return "name: " + name + "\n" +
            "count: " + ((count < 0) ? "ALL" : Integer.toString(count)) + "\n" +
            super.toString();
    }

    public String statementToString() {
        return "FETCH";
    }

}
