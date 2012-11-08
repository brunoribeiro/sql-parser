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

   Derby - Class org.apache.derby.impl.sql.compile.CurrentOfNode

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
 * The CurrentOf operator is used by positioned DELETE 
 * and UPDATE to get the current row and location
 * for the target cursor.    The bind() operations for 
 * positioned DELETE and UPDATE add a column to 
 * the select list under the statement for the row location 
 * accessible from this node.
 *
 * This node is placed in the from clause of the select
 * generated for the delete or update operation. It acts
 * much like a FromBaseTable, using the information about
 * the target table of the cursor to provide information.
 *
 */
public class CurrentOfNode extends FromTable 
{
    private String cursorName;

    //
    // initializer
    //
    public void init(Object correlationName, Object cursor, Object tableProperties) {
        super.init(correlationName, tableProperties);
        cursorName = (String)cursor;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        CurrentOfNode other = (CurrentOfNode)node;
        this.cursorName = other.cursorName;
    }

    /**
     * Prints the sub-nodes of this object.  See QueryTreeNode.java for
     * how tree printing is supposed to work.
     *
     * @param depth The depth of this node in the tree
     */
    public void printSubNodes(int depth) {
        super.printSubNodes(depth);

        printLabel(depth, "cursor: ");
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */
    public String toString() {
        return "preparedStatement: " +
            cursorName + "\n" +
            super.toString();
    }

    public String getCursorName() {
        return cursorName;
    }

}
