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

   Derby - Class org.apache.derby.impl.sql.compile.CharTypeCompiler

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

package com.akiban.sql.compiler;

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.types.DataTypeDescriptor;
import com.akiban.sql.types.TypeId;

/**
 * This class implements TypeCompiler for the SQL char datatypes.
 *
 */

public final class CharTypeCompiler extends TypeCompiler
{
    protected CharTypeCompiler(TypeId typeId) {
        super(typeId);
    }

    /**
     * Tell whether this type (char) can be converted to the given type.
     *
     * @see TypeCompiler#convertible
     */
    public boolean convertible(TypeId otherType, boolean forDataTypeFunction) {
        if (otherType.isAnsiUDT()) { 
            return false; 
        }
                        
        // LONGVARCHAR can only be converted from    character types
        // or CLOB or boolean.
        if (getTypeId().isLongVarcharTypeId()) {
            return (otherType.isStringTypeId() || otherType.isBooleanTypeId());
        }

        // The double function can convert CHAR and VARCHAR
        if (forDataTypeFunction && otherType.isDoubleTypeId())
            return (getTypeId().isStringTypeId());

        // can't CAST to CHAR and VARCHAR from REAL or DOUBLE
        // or binary types or XML
        // all other types are ok.
        if (otherType.isFloatingPointTypeId() || otherType.isBitTypeId() ||
            otherType.isBlobTypeId() || otherType.isXMLTypeId())
            return false;

        return true;
    }

    /**
     * Tell whether this type (char) is compatible with the given type.
     *
     * @param otherType The TypeId of the other type.
     */
    public boolean compatible(TypeId otherType) {
        return (otherType.isStringTypeId() || 
                (otherType.isDateTimeTimeStampTypeId() && 
                 !getTypeId().isLongVarcharTypeId()));
    }

    /**
     * @see TypeCompiler#getCorrespondingPrimitiveTypeName
     */

    public String getCorrespondingPrimitiveTypeName() {
        /* Only numerics and booleans get mapped to Java primitives */
        return "java.lang.String";
    }

    /**
     * Get the method name for getting out the corresponding primitive
     * Java type.
     *
     * @return String The method call name for getting the
     *                              corresponding primitive Java type.
     */
    public String getPrimitiveMethodName() {
        return "getString";
    }

    /**
     * @see TypeCompiler#getCastToCharWidth
     */
    public int getCastToCharWidth(DataTypeDescriptor dts) {
        return dts.getMaximumWidth();
    }

}
