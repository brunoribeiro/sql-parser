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
 * BEGIN / COMMIT / ROLLBACK.
 *
 */
public class TransactionControlNode extends TransactionStatementNode
{
    public static enum StatementType {
        BEGIN, COMMIT, ROLLBACK
    }
    private StatementType statementType;

    /**
     * Initializer for a TransactionControlNode
     *
     * @param transactionStatementType Type of statement.
     *
     * @exception StandardException Thrown on error
     */

    public void init(Object statementType)
            throws StandardException {
        this.statementType = (StatementType)statementType;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        TransactionControlNode other = (TransactionControlNode)node;
        this.statementType = other.statementType;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return super.toString() + 
            "statementType: " + statementType + "\n";
    }

    public String statementToString() {
        switch (statementType) {
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
