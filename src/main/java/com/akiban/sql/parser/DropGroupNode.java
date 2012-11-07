
package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

public class DropGroupNode extends DDLStatementNode {

    private ExistenceCheck existenceCheck;

    public void init(Object dropObjectName, Object ec)
            throws StandardException {
        initAndCheck(dropObjectName);
        this.existenceCheck = (ExistenceCheck)ec;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        DropGroupNode other = (DropGroupNode)node;
        this.existenceCheck = other.existenceCheck;
    }

    public ExistenceCheck getExistenceCheck()
    {
        return existenceCheck;
    }

    @Override
    public String statementToString() {
        return "DROP GROUP";
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */
    public String toString() {
        return super.toString() +
           "existenceCheck: " + existenceCheck + "\n";
    }

}
