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

/**
 * A SET statement for a non-standard configuration variable.
 */

public class SetConfigurationNode extends StatementNode
{
    private String variable, value;

    /**
     * Initializer for SetTransactionIsolationNode
     *
     * @param current Whether applies to current transaction or session default
     * @param isolationLevel The new isolation level
     */
    public void init(Object variable,
                     Object value) {
        this.variable = (String)variable;
        this.value = (String)value;
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

    public String getValue() {
        return value;
    }

    public String statementToString() {
        return "SET " + variable;
    }

}
