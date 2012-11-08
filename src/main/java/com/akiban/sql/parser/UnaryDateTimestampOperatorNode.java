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

   Derby - Class org.apache.derby.impl.sql.compile.UnaryDateTimestampOperatorNode

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
import com.akiban.sql.types.DataTypeDescriptor;

import java.sql.Types;

/**
 * This class implements the timestamp(x) and date(x) functions.
 *
 * These two functions implement a few special cases of string conversions beyond the normal string to
 * date/timestamp casts.
 */
public class UnaryDateTimestampOperatorNode extends UnaryOperatorNode
{
    private static final String DATE_METHOD_NAME = "date";
    private static final String TIME_METHOD_NAME = "time";
    private static final String TIMESTAMP_METHOD_NAME = "timestamp";
        
    /**
     * @param operand The operand of the function
     * @param targetType The type of the result. Timestamp or Date.
     *
     * @exception StandardException Thrown on error
     */

    public void init(Object operand, Object targetType) throws StandardException {
        setType((DataTypeDescriptor)targetType);
        switch(getType().getJDBCTypeId()) {
        case Types.DATE:
            super.init(operand, "date", DATE_METHOD_NAME);
            break;

        case Types.TIME:
            super.init(operand, "time", TIME_METHOD_NAME);
            break;

        case Types.TIMESTAMP:
            super.init(operand, "timestamp", TIMESTAMP_METHOD_NAME);
            break;

        default:
            assert false;
            super.init(operand);
        }
    }
        
}
