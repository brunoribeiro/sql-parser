/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
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

    public void init(Object constraintName, 
                     Object refTableName, 
                     Object fkRcl,
                     Object refRcl,
                     Object refActions) {
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

}
