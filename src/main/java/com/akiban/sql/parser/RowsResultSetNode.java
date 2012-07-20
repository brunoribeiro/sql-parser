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

    public String statementToString() {
        return "VALUES";
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        RowsResultSetNode other = (RowsResultSetNode)node;
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
