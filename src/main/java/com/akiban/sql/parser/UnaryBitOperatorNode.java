/**
 * Copyright (C) 2012 Akiban Technologies Inc.
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
 * This node represents a unary bit operator
 * As of now, there is only one such operator: BITNOT.
 *
 */

public class UnaryBitOperatorNode extends UnaryOperatorNode
{
    /**
     * Initializer for a UnaryBitOperatorNode
     *
     * @param operand The operand of the node
     */
    public void init(Object operand) throws StandardException {
        init(operand, "~", "bitnot");
    }
        
    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);
    }

}
