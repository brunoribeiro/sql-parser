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

   Derby - Class org.apache.derby.impl.sql.compile.TableElementNode

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
 * A TableElementNode is an item in a TableElementList, and represents
 * a single table element such as a column or constraint in a CREATE TABLE
 * or ALTER TABLE statement.
 *
 */

public class TableElementNode extends QueryTreeNode
{
    public static enum ElementType {
        AT_UNKNOWN, AT_ADD_FOREIGN_KEY_CONSTRAINT, AT_ADD_PRIMARY_KEY_CONSTRAINT,
        AT_ADD_UNIQUE_CONSTRAINT, AT_ADD_CHECK_CONSTRAINT, AT_DROP_CONSTRAINT,
        AT_MODIFY_COLUMN, AT_DROP_COLUMN, AT_DROP_INDEX, AT_ADD_INDEX,
        AT_RENAME
    }

    String name;
    // TODO: I don't think the following comment was actually realized;
    // there are still subclasses and this one isn't made directly that
    // I can see.
    ElementType elementType;            // simple element nodes can share this class,
    // eg., drop column and rename table/column/index
    // etc., no need for more classes, an effort to
    // minimize footprint

    /**
     * Initializer for a TableElementNode
     *
     * @param name The name of the table element, if any
     */

    public void init(Object name) {
        this.name = (String)name;
    }

    /**
     * Initializer for a TableElementNode
     *
     * @param name The name of the table element, if any
     */

    public void init(Object name, Object elementType) {
        this.name = (String)name;
        this.elementType = (ElementType)elementType;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        TableElementNode other = (TableElementNode)node;
        this.name = other.name;
        this.elementType = other.elementType;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
     */

    public String toString() {
        return "name: " + name + "\n" +
            "elementType: " + getElementType() + "\n" +
            super.toString();
    }

    /**
     * Does this element have a primary key constraint.
     *
     * @return boolean Whether or not this element has a primary key constraint
     */
    boolean hasPrimaryKeyConstraint() {
        return false;
    }

    /**
     * Does this element have a unique key constraint.
     *
     * @return boolean Whether or not this element has a unique key constraint
     */
    boolean hasUniqueKeyConstraint() {
        return false;
    }

    /**
     * Does this element have a foreign key constraint.
     *
     * @return boolean Whether or not this element has a foreign key constraint
     */
    boolean hasForeignKeyConstraint() {
        return false;
    }

    /**
     * Does this element have a check constraint.
     *
     * @return boolean Whether or not this element has a check constraint
     */
    boolean hasCheckConstraint() {
        return false;
    }

    /**
     * Does this element have a constraint on it.
     *
     * @return boolean Whether or not this element has a constraint on it
     */
    boolean hasConstraint() {
        return false;
    }

    /**
     * Get the name from this node.
     *
     * @return String The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the type of this table element.
     *
     * @return one of the constants at the front of this file
     */
    ElementType getElementType() {
        if (hasForeignKeyConstraint()) {
            return ElementType.AT_ADD_FOREIGN_KEY_CONSTRAINT; 
        }
        else if (hasPrimaryKeyConstraint()) {
            return ElementType.AT_ADD_PRIMARY_KEY_CONSTRAINT; 
        }
        else if (hasUniqueKeyConstraint()) { 
            return ElementType.AT_ADD_UNIQUE_CONSTRAINT; 
        }
        else if (hasCheckConstraint()) { 
            return ElementType.AT_ADD_CHECK_CONSTRAINT; 
        }
        else if (this instanceof ConstraintDefinitionNode) { 
            return ElementType.AT_DROP_CONSTRAINT; 
        }
        else if (this instanceof ModifyColumnNode) {
            if (getNodeType() == NodeTypes.DROP_COLUMN_NODE) { 
                return ElementType.AT_DROP_COLUMN; 
            }
            else { 
                return ElementType.AT_MODIFY_COLUMN; 
            }
        }
        else { 
            return elementType; 
        }
    }

}
