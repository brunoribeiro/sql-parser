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
 * BEGIN / COMMIT / ROLLBACK.
 *
 */
public class TransactionControlNode extends TransactionStatementNode
{
    public static enum Operation {
        BEGIN, COMMIT, ROLLBACK
    }
    private Operation operation;

    /**
     * Initializer for a TransactionControlNode
     *
     * @param transactionOperation Type of statement.
     *
     * @exception StandardException Thrown on error
     */

    public void init(Object operation)
            throws StandardException {
        this.operation = (Operation)operation;
    }

    public Operation getOperation() {
        return operation;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        TransactionControlNode other = (TransactionControlNode)node;
        this.operation = other.operation;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return super.toString() + 
            "operation: " + operation + "\n";
    }

    public String statementToString() {
        switch (operation) {
        case BEGIN:
            return "BEGIN";
        case COMMIT:
            return "COMMIT";
        case ROLLBACK:
            return "ROLLBACK";
        default:
            assert false : "Unknown transaction statement type";
            return "UNKNOWN";
        }
    }

}
