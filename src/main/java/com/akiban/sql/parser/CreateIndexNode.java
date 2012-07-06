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

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.impl.sql.compile.CreateIndexNode

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to you under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.akiban.sql.parser;

import com.akiban.sql.parser.JoinNode.JoinType;

import com.akiban.sql.StandardException;

import java.util.Properties;

/**
 * A CreateIndexNode is the root of a QueryTree that represents a CREATE INDEX
 * statement.
 *
 */

public class CreateIndexNode extends DDLStatementNode
{
    boolean unique;
    String indexType;
    TableName indexName;
    TableName tableName;
    IndexColumnList columnList;
    JoinType joinType;
    Properties properties;
    ExistenceCheck existenceCheck;

    /**
     * Initializer for a CreateIndexNode
     *
     * @param unique True means it's a unique index
     * @param indexType The type of index
     * @param indexName The name of the index
     * @param tableName The name of the table the index will be on
     * @param columnList A list of columns, in the order they
     *                   appear in the index.
     * @param properties The optional properties list associated with the index.
     *
     * @exception StandardException Thrown on error
     */
    public void init(Object unique,
                     Object indexType,
                     Object indexName,
                     Object tableName,
                     Object columnList,
                     Object joinType,
                     Object properties,
                     Object existenceCheck) 
            throws StandardException {
        initAndCheck(indexName);
        this.unique = ((Boolean)unique).booleanValue();
        this.indexType = (String)indexType;
        this.indexName = (TableName)indexName;
        this.tableName = (TableName)tableName;
        this.columnList = (IndexColumnList)columnList;
        this.joinType = (JoinType)joinType;
        this.properties = (Properties)properties;
        this.existenceCheck = (ExistenceCheck)existenceCheck;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        CreateIndexNode other = (CreateIndexNode)node;
        this.unique = other.unique;
        this.indexType = other.indexType;
        this.indexName = (TableName)
            getNodeFactory().copyNode(other.indexName, getParserContext());
        this.tableName = (TableName)
            getNodeFactory().copyNode(other.tableName, getParserContext());
        this.columnList = (IndexColumnList)
            getNodeFactory().copyNode(other.columnList, getParserContext());
        this.joinType = other.joinType;
        this.properties = other.properties; // TODO: Clone?
        this.existenceCheck = other.existenceCheck;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return super.toString() +
            "unique: " + unique + "\n" +
            "indexType: " + indexType + "\n" +
            "indexName: " + indexName + "\n" +
            "tableName: " + tableName + "\n" +
            "joinType: " + joinType + "\n" +
            "properties: " + properties + "\n" +
            "existenceCheck: " + existenceCheck + "\n";
    }

    public void printSubNodes(int depth) {
        if (columnList != null) {
            columnList.treePrint(depth+1);
        }
    }
    public String statementToString() {
        return "CREATE INDEX";
    }

    public boolean getUniqueness() { 
        return unique; 
    }
    public String getIndexType() { 
        return indexType;
    }
    public TableName getIndexName() { 
        return indexName; 
    }
    public IndexColumnList getColumnList() {
        return columnList;
    }
    public JoinType getJoinType() {
        return joinType;
    }
    public Properties getProperties() { 
        return properties; 
    }
    public TableName getIndexTableName() {
        return tableName; 
    }

    public ExistenceCheck getExistenceCheck()
    {
        return existenceCheck;
    }
}
