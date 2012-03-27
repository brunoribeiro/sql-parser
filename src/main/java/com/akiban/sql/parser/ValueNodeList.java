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

   Derby - Class org.apache.derby.impl.sql.compile.ValueNodeList

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
 * A ValueNodeList represents a list of ValueNodes within a specific predicate 
 * (eg, IN list, NOT IN list or BETWEEN) in a DML statement.    
 * It extends QueryTreeNodeList.
 *
 */

public class ValueNodeList extends QueryTreeNodeList<ValueNode>
{
    /**
     * Add a ValueNode to the list.
     *
     * @param valueNode A ValueNode to add to the list
     *
     * @exception StandardException     Thrown on error
     */

    public void addValueNode(ValueNode valueNode) throws StandardException {
        add(valueNode);
    }

    /**
     * Check if all the elements in this list are equivalent to the elements
     * in another list. The two lists must have the same size, and the
     * equivalent nodes must appear in the same order in both lists, for the
     * two lists to be equivalent.
     *
     * @param other the other list
     * @return {@code true} if the two lists contain equivalent elements, or
     * {@code false} otherwise
     * @throws StandardException thrown on error
     * @see ValueNode#isEquivalent(ValueNode)
     */
    boolean isEquivalent(ValueNodeList other) throws StandardException {
        if (size() != other.size()) {
            return false;
        }

        for (int i = 0; i < size(); i++) {
            ValueNode vn1 = get(i);
            ValueNode vn2 = other.get(i);
            if (!vn1.isEquivalent(vn2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Return whether or not this expression tree represents a constant expression.
     *
     * @return Whether or not this expression tree represents a constant expression.
     */
    public boolean isConstantExpression() {
        int size = size();

        for (int index = 0; index < size; index++) {
            boolean retcode;
            retcode = get(index).isConstantExpression();
            if (!retcode) {
                return retcode;
            }
        }

        return true;
    }

}
