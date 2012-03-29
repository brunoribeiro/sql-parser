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

   Derby - Class org.apache.derby.impl.sql.compile.FromTable

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

import com.akiban.sql.StandardException;

import java.util.Properties;

/**
 * A FromTable represents a table in the FROM clause of a DML statement.
 * It can be either a base table, a subquery or a project restrict.
 *
 * @see FromBaseTable
 * @see FromSubquery
 * @see ProjectRestrictNode
 *
 */
public abstract class FromTable extends ResultSetNode
{
    protected Properties tableProperties;
    protected String correlationName;
    private TableName corrTableName;

    /** the original unbound table name */
    // TODO: Still need these two separate names?
    protected TableName origTableName;

    /**
     * Initializer for a table in a FROM list.
     *
     * @param correlationName The correlation name
     * @param tableProperties Properties list associated with the table
     */
    public void init(Object correlationName, Object tableProperties) {
        this.correlationName = (String)correlationName;
        this.tableProperties = (Properties)tableProperties;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        FromTable other = (FromTable)node;
        this.tableProperties = other.tableProperties; // TODO: Clone?
        this.correlationName = other.correlationName;
        this.corrTableName = (TableName)getNodeFactory().copyNode(other.corrTableName,
                                                                  getParserContext());
        this.origTableName = (TableName)getNodeFactory().copyNode(other.origTableName,
                                                                  getParserContext());
    }

    /**
     * Get this table's correlation name, if any.
     */
    public String getCorrelationName() { 
        return correlationName; 
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return "correlation Name: " + correlationName + "\n" +
            (corrTableName != null ?
             corrTableName.toString() : "null") + "\n" +
            super.toString();
    }

    /**
     * Return a TableName node representing this FromTable.
     * Expect this to be overridden (and used) by subclasses
     * that may set correlationName to null.
     *
     * @return a TableName node representing this FromTable.
     * @exception StandardException Thrown on error
     */
    public TableName getTableName() throws StandardException {
        if (correlationName == null) return null;

        if (corrTableName == null) {
            corrTableName = makeTableName(null, correlationName);
        }
        return corrTableName;
    }

    public String getExposedName() throws StandardException {
        return null;
    }

    /**
     * Sets the original or unbound table name for this FromTable.  
     * 
     * @param tableName the unbound table name
     *
     */
    public void setOrigTableName(TableName tableName) {
        this.origTableName = tableName;
    }

    /**
     * Gets the original or unbound table name for this FromTable.  
     * The tableName field can be changed due to synonym resolution.
     * Use this method to retrieve the actual unbound tablename.
     * 
     * @return TableName the original or unbound tablename
     *
     */
    public TableName getOrigTableName() {
        return this.origTableName;
    }

}
