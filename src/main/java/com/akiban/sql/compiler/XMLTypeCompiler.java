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

   Derby - Class org.apache.derby.impl.sql.compile.XMLTypeCompiler

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
 * This class implements TypeCompiler for the XML type.
 */

public class XMLTypeCompiler extends TypeCompiler
{
    protected XMLTypeCompiler(TypeId typeId) {
        super(typeId);
    }

    /**
     * Tell whether this type (XML) can be converted to the given type.
     *
     * An XML value can't be converted to any other type, per
     * SQL/XML[2003] 6.3 <cast specification>
     *
     * @see TypeCompiler#convertible
     */
    public boolean convertible(TypeId otherType, boolean forDataTypeFunction) {
        // An XML value cannot be converted to any non-XML type.    If
        // user wants to convert an XML value to a string, then
        // s/he must use the provided SQL/XML serialization operator
        // (namely, XMLSERIALIZE).
        return otherType.isXMLTypeId();
    }

    /**
     * Tell whether this type (XML) is compatible with the given type.
     *
     * @param otherType The TypeId of the other type.
     */
    public boolean compatible(TypeId otherType) {
        // An XML value is not compatible (i.e. cannot be "coalesced")
        // into any non-XML type.
        return otherType.isXMLTypeId();
    }

    /**
     * @see TypeCompiler#getCorrespondingPrimitiveTypeName
     */
    public String getCorrespondingPrimitiveTypeName() {
        int formatId = getStoredFormatIdFromTypeId();
        if (formatId == TypeId.FormatIds.XML_TYPE_ID)
            return "org.apache.derby.iapi.types.XML"; // TODO: What is really used?

        assert false : "unexpected formatId in getCorrespondingPrimitiveTypeName(): " + formatId;
        return null;
    }

    /**
     * Get the method name for getting out the corresponding primitive
     * Java type.
     *
     * @return String The method call name for getting the
     *                              corresponding primitive Java type.
     */
    public String getPrimitiveMethodName() {
        return "getXML";                        // TODO: How does this really work?
    }

    /**
     * @see TypeCompiler#getCastToCharWidth
     *
     * While it is true XML values can't be cast to char, this method
     * can get called before we finish type checking--so we return a dummy
     * value here and let the type check throw the appropriate error.
     */
    public int getCastToCharWidth(DataTypeDescriptor dts) {
        return -1;
    }

}
