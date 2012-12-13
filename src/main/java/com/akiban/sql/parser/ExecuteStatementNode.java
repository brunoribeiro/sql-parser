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
 * EXECUTE a previously prepare statement.
 */

public class ExecuteStatementNode extends StatementNode
{
    private String name;
    private ValueNodeList parameterList;

    /**
     * Initializer for an ExecuteStatementNode
     *
     * @param name The name of the prepared statement.
     * @param parameterList Any parameter values to be bound.
     */

    public void init(Object name,
                     Object parameterList) {
        this.name = (String)name;
        this.parameterList = (ValueNodeList)parameterList;
    }

    public String getName() {
        return name;
    }

    public ValueNodeList getParameterList() {
        return parameterList;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);
        
        ExecuteStatementNode other = (ExecuteStatementNode)node;
        this.name = other.name;
        this.parameterList = (ValueNodeList)
            getNodeFactory().copyNode(other.parameterList, getParserContext());
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return "name: " + name + "\n" +
            super.toString();
    }

    public String statementToString() {
        return "EXECUTE";
    }

    /**
     * Prints the sub-nodes of this object.  See QueryTreeNode.java for
     * how tree printing is supposed to work.
     *
     * @param depth The depth of this node in the tree
     */

    public void printSubNodes(int depth) {
        super.printSubNodes(depth);

        printLabel(depth, "parameterList: ");
        parameterList.treePrint(depth + 1);
    }

    /**
     * Accept the visitor for all visitable children of this node.
     *
     * @param v the visitor
     * @throws StandardException on error in the visitor
     */
    void acceptChildren(Visitor v) throws StandardException {
        super.acceptChildren(v);

        parameterList = (ValueNodeList)parameterList.accept(v);
    }

}
