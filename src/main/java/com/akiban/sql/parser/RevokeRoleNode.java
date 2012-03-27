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

   Derby - Class org.apache.derby.impl.sql.compile.RevokeRoleNode

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
 * This class represents a REVOKE role statement.
 */
public class RevokeRoleNode extends DDLStatementNode
{
    private List<String> roles;
    private List<String> grantees;

    /**
     * Initialize a RevokeRoleNode.
     *
     * @param roles list of strings containing role name to be revoked
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

        RevokeRoleNode other = (RevokeRoleNode)node;
        this.roles = other.roles;
        this.grantees = other.grantees;
    }

    /**
     * Convert this object to a String.  See comments in QueryTreeNode.java
     * for how this should be done for tree printing.
     *
     * @return This object as a String
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
                " FROM: " +
                sb2.toString() +
                "\n");
    }

    public String statementToString() {
        return "REVOKE role";
    }

}
