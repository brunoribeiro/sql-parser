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

   Derby - Class org.apache.derby.impl.sql.compile.GrantRoleNode

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

import java.util.List;
import java.util.Iterator;

/**
 * This class represents a GRANT role statement.
 */
public class GrantRoleNode extends DDLStatementNode
{
    private List<String> roles;
    private List<String> grantees;

    /**
     * Initialize a GrantRoleNode.
     *
     * @param roles list of strings containing role name to be granted
     * @param grantees list of strings containing grantee names
     */
    public void init(Object roles, Object grantees) throws StandardException {
        initAndCheck(null);
        this.roles = (List<String>)roles;
        this.grantees = (List<String>)grantees;
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        GrantRoleNode other = (GrantRoleNode)node;
        this.roles = other.roles;           // TODO: Clone?
        this.grantees = other.grantees; // Ditto
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return  This object as a String
     */

    public String toString() {
        StringBuffer sb1 = new StringBuffer();
        for (Iterator<String> it = roles.iterator(); it.hasNext();) {
            if (sb1.length() > 0) {
                sb1.append(", ");
            }
            sb1.append(it.next());
        }

        StringBuffer sb2 = new StringBuffer();
        for (Iterator<String> it = grantees.iterator(); it.hasNext();) {
            if (sb2.length() > 0) {
                sb2.append(", ");
            }
            sb2.append(it.next());
        }
        return (super.toString() +
                sb1.toString() +
                " TO: " +
                sb2.toString() +
                "\n");
    }

    public String statementToString() {
        return "GRANT role";
    }

}
