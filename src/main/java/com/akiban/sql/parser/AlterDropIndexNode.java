
package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

public class AlterDropIndexNode extends TableElementNode
{
    private ExistenceCheck existenceCheck;

    @Override
    public void init(Object indexName,
                     Object ec)
    {
        super.init(indexName, ElementType.AT_DROP_INDEX);
        this.existenceCheck = (ExistenceCheck)ec;
    }

    public String getIndexName()
    {
        return name;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void copyFrom(QueryTreeNode node) throws StandardException 
    {
        super.copyFrom(node);

        AlterDropIndexNode other = (AlterDropIndexNode)node;
        this.existenceCheck = other.existenceCheck;
    }

    @Override
    public String toString()
    {
        return super.toString()
               + "\nexistenceCheck: " + existenceCheck;
    }

    public String statementToString()
    {
        return "ALTER TABLE DROP INDEX";
    }

    public ExistenceCheck getExistenceCheck()
    {
        return existenceCheck;
    }
}
