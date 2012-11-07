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

import java.util.ArrayList;
import java.util.List;

/**
 * A RowsResultSetNode represents the result set for a multi row VALUES clause.
 *
 */

public class RowsResultSetNode extends FromTable
{
    private List<RowResultSetNode> rows;

    /**
     * Initializer for a RowsResultSetNode.
     *
     * @param firstRow The initial row.
     */
    public void init(Object firstRow) throws StandardException {
        super.init(null, tableProperties);
        RowResultSetNode row = (RowResultSetNode)firstRow;
        rows = new ArrayList<RowResultSetNode>();
        rows.add(row);
        resultColumns = (ResultColumnList)
            getNodeFactory().copyNode(row.getResultColumns(), getParserContext());
    }

    public List<RowResultSetNode> getRows() {
        return rows;
    }

    public void addRow(RowResultSetNode row) {
        rows.add(row);
    }

    public String statementToString() {
        return "VALUES";
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        RowsResultSetNode other = (RowsResultSetNode)node;
        rows = new ArrayList<RowResultSetNode>(other.rows.size());
        for (RowResultSetNode row : other.rows)
            rows.add((RowResultSetNode)getNodeFactory().copyNode(row, getParserContext()));
    }

    /**
     * Prints the sub-nodes of this object.  See QueryTreeNode.java for
     * how tree printing is supposed to work.
     *
     * @param depth The depth of this node in the tree
     */

    public void printSubNodes(int depth) {
        super.printSubNodes(depth);
        for (int index = 0; index < rows.size(); index++) {
            debugPrint(formatNodeString("[" + index + "]:", depth));
            RowResultSetNode row = rows.get(index);
            row.treePrint(depth);
        }
    }

    /**
     * Accept the visitor for all visitable children of this node.
     * 
     * @param v the visitor
     *
     * @exception StandardException on error
     */
    void acceptChildren(Visitor v) throws StandardException {
        super.acceptChildren(v);

        int size = rows.size();
        for (int index = 0; index < size; index++) {
            rows.set(index, (RowResultSetNode)rows.get(index).accept(v));
        }
    }

}
