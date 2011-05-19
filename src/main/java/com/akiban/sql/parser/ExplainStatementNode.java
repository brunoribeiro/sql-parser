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
 * An ExplainStatementNode represents the EXPLAIN command.
 *
 */

public class ExplainStatementNode extends StatementNode
{
    private StatementNode statement;

    /**
     * Initializer for an ExplainStatementNode
     *
     * @param statemen The statement to be explained.
     */

    public void init(Object statement) {
        this.statement = (StatementNode)statement;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);
        
        ExplainStatementNode other = (ExplainStatementNode)node;
        this.statement = (StatementNode)getNodeFactory().copyNode(other.statement,
                                                                  getParserContext());
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return super.toString();
    }

    public String statementToString() {
        return "EXPLAIN";
    }

    /**
     * Prints the sub-nodes of this object.  See QueryTreeNode.java for
     * how tree printing is supposed to work.
     *
     * @param depth The depth of this node in the tree
     */

    public void printSubNodes(int depth) {
        super.printSubNodes(depth);

        printLabel(depth, "statement: ");
        statement.treePrint(depth + 1);
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

        statement = (StatementNode)statement.accept(v);
    }

    public StatementNode getStatement() {
        return statement;
    }

}
