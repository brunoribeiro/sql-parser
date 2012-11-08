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

   Derby - Class org.apache.derby.impl.sql.compile.LengthOperatorNode

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

import com.akiban.sql.types.TypeId;

import java.sql.Types;

/**
 * This node represents a unary XXX_length operator
 *
 */

public final class LengthOperatorNode extends UnaryOperatorNode
{
    private int parameterType;
    private int parameterWidth;

    public void setNodeType(int nodeType) {
        String operator = null;
        String methodName = null;

        if (nodeType == NodeTypes.CHAR_LENGTH_OPERATOR_NODE) {
            operator = "char_length";
            methodName = "charLength";
            parameterType = Types.VARCHAR;
            parameterWidth = TypeId.VARCHAR_MAXWIDTH;
        }
        else {
            assert false : "Unexpected nodeType = " + nodeType;
        }
        setOperator(operator);
        setMethodName(methodName);
        super.setNodeType(nodeType);
    }

}
