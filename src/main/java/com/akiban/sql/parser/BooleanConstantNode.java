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

   Derby - Class org.apache.derby.impl.sql.compile.BooleanConstantNode

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
import com.akiban.sql.types.TypeId;

import java.sql.Types;

public class BooleanConstantNode extends ConstantNode
{
    private boolean booleanValue;
    private boolean unknownValue;

    /**
     * Initializer for a BooleanConstantNode.
     *
     * @param arg1 A boolean containing the value of the constant OR The TypeId for the type of the node
     *
     * @exception StandardException
     */
    public void init(Object arg1) throws StandardException {
        /*
        ** RESOLVE: The length is fixed at 1, even for nulls.
        ** Is that OK?
        */

        if (arg1 == null) {
            /* Fill in the type information in the parent ValueNode */
            super.init(TypeId.BOOLEAN_ID,
                       Boolean.TRUE,
                       1);

            setValue(null);
        }
        else if (arg1 instanceof Boolean) {
            /* Fill in the type information in the parent ValueNode */
            super.init(TypeId.BOOLEAN_ID,
                       Boolean.FALSE,
                       1);

            booleanValue = ((Boolean)arg1).booleanValue();
            super.setValue(arg1);
        }
        else {
            super.init(arg1,
                       Boolean.TRUE,
                       0);
            unknownValue = true;
        }
    }

    /**
     * Fill this node with a deep copy of the given node.
     */
    public void copyFrom(QueryTreeNode node) throws StandardException {
        super.copyFrom(node);

        BooleanConstantNode other = (BooleanConstantNode)node;
        this.booleanValue = other.booleanValue;
        this.unknownValue = other.unknownValue;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    /**
     * Return an Object representing the bind time value of this
     * expression tree.  If the expression tree does not evaluate to
     * a constant at bind time then we return null.
     * This is useful for bind time resolution of VTIs.
     * RESOLVE: What do we do for primitives?
     *
     * @return An Object representing the bind time value of this expression tree.
     *               (null if not a bind time constant.)
     *
     */
    Object getConstantValueAsObject() {
        return booleanValue ? Boolean.TRUE : Boolean.FALSE;
    }

    /**
     * Return the value as a string.
     *
     * @return The value as a string.
     *
     */
    String getValueAsString() {
        if (booleanValue) {
            return "true";
        }
        else {
            return "false";
        }
    }

    /**
     * Does this represent a true constant.
     *
     * @return Whether or not this node represents a true constant.
     */
    public boolean isBooleanTrue() {
        return (booleanValue && !unknownValue);
    }

    /**
     * Does this represent a false constant.
     *
     * @return Whether or not this node represents a false constant.
     */
    public boolean isBooleanFalse() {
        return (!booleanValue && !unknownValue);
    }

}
