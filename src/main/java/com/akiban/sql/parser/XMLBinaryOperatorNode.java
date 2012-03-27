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

   Derby - Class org.apache.derby.impl.sql.compile.BinaryOperatorNode

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
import com.akiban.sql.types.ValueClassName;

/**
 * A BinaryOperatorNode represents a built-in binary operator as defined by
 * the ANSI/ISO SQL standard.    This covers operators like +, -, *, /, =, <, etc.
 * Java operators are not represented here: the JSQL language allows Java
 * methods to be called from expressions, but not Java operators.
 *
 */

public class XMLBinaryOperatorNode extends BinaryOperatorNode
{
    // Derby did the following, which just make things too messy:
    //   At the time of adding XML support, it was decided that
    //   we should avoid creating new OperatorNodes where possible.
    //   So for the XML-related binary operators we just add the
    //   necessary code to _this_ class, similar to what is done in
    //   TernarnyOperatorNode. Subsequent binary operators (whether
    //   XML-related or not) should follow this example when
    //   possible.

    public static enum OperatorType {
        EXISTS("xmlexists", "XMLExists",
               ValueClassName.BooleanDataValue,
               new String[] { ValueClassName.StringDataValue, ValueClassName.XMLDataValue }),
        QUERY("xmlquery", "XMLQuery", 
              ValueClassName.XMLDataValue,
              new String [] { ValueClassName.StringDataValue, ValueClassName.XMLDataValue });

        String operator, methodName;
        String resultType;
        String[] argTypes;
        OperatorType(String operator, String methodName,
                     String resultType, String[] argTypes) {
            this.operator = operator;
            this.methodName = methodName;
            this.resultType = resultType;
            this.argTypes = argTypes;
        }
    }

    public static enum PassByType {
        REF, VALUE
    }
    public static enum ReturnType {
        SEQUENCE, CONTENT
    }
    public static enum OnEmpty {
        EMPTY, NULL
    }

    /**
     * Initializer for a BinaryOperatorNode
     *
     * @param leftOperand The left operand of the node
     * @param rightOperand The right operand of the node
     * @param opType    An Integer holding the operatorType
     *  for this operator.
     */
    public void init(Object leftOperand,
                     Object rightOperand,
                     Object opType) {
        this.leftOperand = (ValueNode)leftOperand;
        this.rightOperand = (ValueNode)rightOperand;
        OperatorType operatorType = (OperatorType)opType;
        this.operator = operatorType.operator;
        this.methodName = operatorType.operator;
        this.leftInterfaceType = operatorType.argTypes[0];
        this.rightInterfaceType = operatorType.argTypes[1];
        this.resultInterfaceType = operatorType.resultType;
    }

}
