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

   Derby - Class org.apache.derby.impl.sql.compile.LikeEscapeOperatorNode

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

/**
        This node represents a like comparison operator (no escape)

        If the like pattern is a constant or a parameter then if possible
        the like is modified to include a >= and < operator. In some cases
        the like can be eliminated.  By adding =, >= or < operators it may
        allow indexes to be used to greatly narrow the search range of the
        query, and allow optimizer to estimate number of rows to affected.


        constant or parameter LIKE pattern with prefix followed by optional wild 
        card e.g. Derby%

        CHAR(n), VARCHAR(n) where n < 255

                >=   prefix padded with '\u0000' to length n -- e.g. Derby\u0000\u0000
                <=   prefix appended with '\uffff' -- e.g. Derby\uffff

                [ can eliminate LIKE if constant. ]


        CHAR(n), VARCHAR(n), LONG VARCHAR where n >= 255

                >= prefix backed up one characer
                <= prefix appended with '\uffff'

                no elimination of like


        parameter like pattern starts with wild card e.g. %Derby

        CHAR(n), VARCHAR(n) where n <= 256

                >= '\u0000' padded with '\u0000' to length n
                <= '\uffff'

                no elimination of like

        CHAR(n), VARCHAR(n), LONG VARCHAR where n > 256

                >= NULL

                <= '\uffff'


        Note that the Unicode value '\uffff' is defined as not a character value
        and can be used by a program for any purpose. We use it to set an upper
        bound on a character range with a less than predicate. We only need a single
        '\uffff' appended because the string 'Derby\uffff\uffff' is not a valid
        String because '\uffff' is not a valid character.

**/

public final class LikeEscapeOperatorNode extends TernaryOperatorNode
{

    /**
     * Initializer for a LikeEscapeOperatorNode
     *
     * receiver like pattern [ escape escapeValue ]
     *
     * @param receiver          The left operand of the like: 
     *                                                          column, CharConstant or Parameter
     * @param leftOperand       The right operand of the like: the pattern
     * @param rightOperand  The optional escape clause, null if not present
     */
    public void init(Object receiver,
                     Object leftOperand,
                     Object rightOperand)
    {
        /* By convention, the method name for the like operator is "like" */
        super.init(receiver, leftOperand, rightOperand, 
                   TernaryOperatorNode.OperatorType.LIKE, null); 
    }

}
