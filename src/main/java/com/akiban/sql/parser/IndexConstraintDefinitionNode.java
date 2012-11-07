
package com.akiban.sql.parser;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.JoinNode.JoinType;

public class IndexConstraintDefinitionNode extends ConstraintDefinitionNode
{
    private String indexName;
    private IndexColumnList indexColumnList;
    private JoinType joinType;
    private StorageLocation location;
    
    @Override
    public void init(Object tableName,
                     Object indexColumnList,
                     Object indexName,
                     Object joinType,
                     Object location)
    {
        super.init(tableName,
                   ConstraintType.INDEX,
                   null, // properties : don't need
                   null, // column list? don't need. Use indexColumnList instead
                   null, // constrainText ? 
                   null, // conditionCheck ?
                   StatementType.UNKNOWN, // behaviour? 
                   ConstraintType.INDEX);
        
        this.indexName = (String) indexName;
        this.indexColumnList = (IndexColumnList) indexColumnList;
        this.joinType = (JoinType) joinType;
        this.location = (StorageLocation) location;
    }
    
    public String getIndexName()
    {
        return indexName;
    }
    
    public IndexColumnList getIndexColumnList()
    {
        return indexColumnList;
    }
    
    public JoinType getJoinType()
    {
        return joinType;
    }
    
    public StorageLocation getLocation()
    {
        return location;
    }
    
    @Override
    public void copyFrom(QueryTreeNode node) throws StandardException
    {
        super.copyFrom(node);
        
        IndexConstraintDefinitionNode other = (IndexConstraintDefinitionNode) node;
        this.indexName = other.indexName;
        this.indexColumnList = other.indexColumnList;
        this.joinType = other.joinType;
        this.location = other.location;
    }
    
    @Override
    public String toString()
    {
        return super.toString()
                + "\nindexName: " + indexName
                + "\nindexColumnList: " + indexColumnList
                + "\njoinType: " + joinType
                + "\nlocation: " + location
                ;
    }
}
