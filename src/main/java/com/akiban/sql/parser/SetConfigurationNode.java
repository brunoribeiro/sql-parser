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
 * A SET statement for a non-standard configuration variable.
 */

public class SetConfigurationNode extends StatementNode
{
    private String variable;
    private Object value;

    /**
     * Initializer for SetTransactionIsolationNode
     *
     * @param current Whether applies to current transaction or session default
     * @param isolationLevel The new isolation level
     */
    public void init(Object variable,
                     Object value) {
        this.variable = (String)variable;
        this.value = value;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        SetConfigurationNode other = (SetConfigurationNode)node;
        this.variable = other.variable;
        this.value = other.value;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return "variable: " + variable + "\n" +
               "value: " + value + "\n" +
            super.toString();
    }

    public String getVariable() {
        return variable;
    }

    public Object getValue() {
        return value;
    }

    public String statementToString() {
        return "SET " + variable;
    }

}
