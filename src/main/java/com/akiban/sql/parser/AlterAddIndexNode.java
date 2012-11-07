
package com.akiban.sql.parser;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.JoinNode.JoinType;
import java.util.Properties;

public class AlterAddIndexNode extends TableElementNode
{
    ExistenceCheck existenceCheck;
    boolean unique;
    IndexColumnList indexColumnList;
    JoinType joinType ;
    Properties properties;
    StorageLocation storageLocation;
    
    @Override
    public void init(Object cond,
                     Object unique,
                     Object indexName,
                     Object indexColumnList,
                     Object joinType,
                     Object properties,
                     Object location)
    {
        super.init(indexName, ElementType.AT_ADD_INDEX);
        
        this.existenceCheck = (ExistenceCheck)cond;
        this.unique = ((Boolean)unique).booleanValue();
        this.indexColumnList = (IndexColumnList) indexColumnList;
        this.joinType = (JoinType) joinType;
        this.properties = (Properties) properties;
        this.storageLocation = (StorageLocation) location;
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

        AlterAddIndexNode other = (AlterAddIndexNode)node;
        this.existenceCheck = other.existenceCheck;
        this.unique = other.unique;
        this.indexColumnList = other.indexColumnList;
        this.joinType = other.joinType;
        this.properties = other.properties;
        this.storageLocation = other.storageLocation;
    }

    @Override
    public String toString()
    {
        return super.toString()
                + "\nexistenceCheck: " + existenceCheck
                + "\nunique: "+ unique
                + "\nindexColumnList: " + indexColumnList
                + "\njoinType: " + joinType
                + "\nproperties: " + properties
                + "\nlocation: " + storageLocation;
    }

    public String statementToString()
    {
        return "ALTER TABLE ADD INDEX";
    }

    public ExistenceCheck getExistenceCheck()
    {
        return existenceCheck;
    }
    
    public boolean isUnique()
    {
        return unique;
    }
    
    public IndexColumnList getIndexColunmList()
    {
        return indexColumnList;
    }
    
    public JoinType getJoinType()
    {
        return joinType;
    }
    
    public Properties getProperties()
    {
        return properties;
    }
    
    public StorageLocation getStorageLocation()
    {
        return storageLocation;
    }
}
