
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
