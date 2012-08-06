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
import com.akiban.sql.parser.JoinNode.JoinType;

public class AddIndexConstraintDefinitionNode extends ConstraintDefinitionNode
{
    private String indexName;
    private IndexColumnList indexColumnList;
    private JoinType joinType;
    private StorageLocation location;
    
    @Override
    public void init(Object tableName,
                     Object properties,
                     Object indexColumnList,
                     Object indexName,
                     Object joinType,
                     Object location)
    {
        super.init(tableName,
                   ConstraintType.ADD_INDEX,
                   properties,
                   null, // column list? don't need. Use indexColumnList instead
                   null, // constrainText ? 
                   null, // conditionCheck ?
                   StatementType.UNKNOWN, // behaviour? 
                   ConstraintType.ADD_INDEX);
        
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
        
        AddIndexConstraintDefinitionNode other = (AddIndexConstraintDefinitionNode) node;
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
