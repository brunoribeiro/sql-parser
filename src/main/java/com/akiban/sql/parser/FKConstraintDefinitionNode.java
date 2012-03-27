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

   Derby - Class org.apache.derby.impl.sql.compile.FKConstraintDefinitionNode

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

/**
 * A FKConstraintDefintionNode represents table constraint definitions.
 *
 */

public class FKConstraintDefinitionNode extends ConstraintDefinitionNode
{
    TableName refTableName;
    ResultColumnList refRcl;
    int refActionDeleteRule;    // referential action on delete
    int refActionUpdateRule;    // referential action on update
    boolean grouping;

    public void init(Object constraintName, 
                     Object refTableName, 
                     Object fkRcl,
                     Object refRcl,
                     Object refActions,
                     Object grouping) {
        super.init(constraintName,
                   ConstraintType.FOREIGN_KEY,
                   fkRcl, 
                   null,
                   null,
                   null);
        this.refRcl = (ResultColumnList)refRcl;
        this.refTableName = (TableName)refTableName;

        this.refActionDeleteRule = ((int[])refActions)[0];
        this.refActionUpdateRule = ((int[])refActions)[1];

        this.grouping = ((Boolean)grouping).booleanValue();
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        FKConstraintDefinitionNode other = (FKConstraintDefinitionNode)node;
        this.refTableName = (TableName)getNodeFactory().copyNode(other.refTableName,
                                                                 getParserContext());
        this.refRcl = (ResultColumnList)getNodeFactory().copyNode(other.refRcl,
                                                                  getParserContext());
        this.refActionDeleteRule = other.refActionDeleteRule;
        this.refActionUpdateRule = other.refActionUpdateRule;
    }

    public TableName getRefTableName() { 
        return refTableName; 
    }

    public ResultColumnList getRefResultColumnList() {
        return refRcl;
    }

    public boolean isGrouping() {
        return grouping;
    }
    
    public String toString() {
        return "refTable name : " + refTableName + "\n" +
            "grouping: " + grouping + "\n" + 
            super.toString();
    }
    

}
