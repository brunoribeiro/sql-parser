/**
 * Copyright © 2012 Akiban Technologies, Inc.  All rights
 * reserved.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact
 * licensing@akiban.com.
 *
 * Contributors:
 * Akiban Technologies, Inc.
 */

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.impl.sql.compile.CurrentSequenceNode

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
 * A class that represents a value obtained from a Sequence using 'CURRENT VALUE'
 */
public class CurrentSequenceNode extends ValueNode 
{
    private TableName sequenceName;

    /**
     * Initializer for a CurrentSequenceNode
     *
     * @param sequenceName The name of the sequence being called
     * @throws StandardException Thrown on error
     */
    public void init(Object sequenceName) throws StandardException {
        this.sequenceName = (TableName)sequenceName;
    }

    public TableName getSequenceName () {
        return sequenceName;
    }
    
    
    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        CurrentSequenceNode other = (CurrentSequenceNode)node;
        this.sequenceName = (TableName)getNodeFactory().copyNode(other.sequenceName,
                                                                 getParserContext());
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return super.toString() +
         "Sequence: " + sequenceName;
    }

    protected boolean isEquivalent(ValueNode other) throws StandardException {
        return false;
    }

    
}
