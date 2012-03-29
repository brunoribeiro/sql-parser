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

   Derby - Class org.apache.derby.impl.sql.compile.TablePrivilegesNode

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
 * This class represents a set of privileges on one table.
 */
public class TablePrivilegesNode extends QueryTreeNode
{
    // Action types
    // TODO: Could be enum, but used as array index below.
    public static final int SELECT_ACTION = 0;
    public static final int DELETE_ACTION = 1;
    public static final int INSERT_ACTION = 2;
    public static final int UPDATE_ACTION = 3;
    public static final int REFERENCES_ACTION = 4;
    public static final int TRIGGER_ACTION = 5;
    public static final int ACTION_COUNT = 6;

    private boolean[] actionAllowed = new boolean[ACTION_COUNT];
    private ResultColumnList[] columnLists = new ResultColumnList[ACTION_COUNT];

    /**
     * Add all actions
     */
    public void addAll() {
        for (int i = 0; i < ACTION_COUNT; i++) {
            actionAllowed[i] = true;
            columnLists[i] = null;
        }
    }

    /**
     * Add one action to the privileges for this table
     *
     * @param action The action type
     * @param privilegeColumnList The set of privilege columns. Null for all columns
     *
     * @exception StandardException standard error policy.
     */
    public void addAction(int action, ResultColumnList privilegeColumnList) {
        actionAllowed[action] = true;
        if (privilegeColumnList == null)
            columnLists[action] = null;
        else if (columnLists[action] == null)
            columnLists[action] = privilegeColumnList;
        else
            columnLists[action].appendResultColumns(privilegeColumnList, false);
    }

}
