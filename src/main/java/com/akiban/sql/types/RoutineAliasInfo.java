/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.catalog.types.RoutineAliasInfo

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.akiban.sql.types;

import java.sql.ParameterMetaData;
import com.akiban.sql.types.DataTypeDescriptor;

/**
 * Describe a routine (procedure or function) alias.
 *
 * @see org.apache.derby.catalog.AliasInfo
 */
public class RoutineAliasInfo extends MethodAliasInfo
{
    // TODO: enum

    private static final String[] SQL_CONTROL = {
        "MODIFIES SQL DATA", "READS SQL DATA", "CONTAINS SQL", "NO SQL"
    };
    public static final short MODIFIES_SQL_DATA = 0;
    public static final short READS_SQL_DATA = 1;
    public static final short CONTAINS_SQL = 2;
    public static final short NO_SQL = 3;

    // TODO: Another enum 

    /** PARAMETER STYLE JAVA */
    public static final short PS_JAVA = 0;

    /** PARAMETER STYLE DERBY_JDBC_RESULT_SET */
    public static final short PS_DERBY_JDBC_RESULT_SET = PS_JAVA + 1;

    /** Masks for the sqlOptions field */
    private static final short SQL_ALLOWED_MASK = (short) 0xF;
    private static final short DETERMINISTIC_MASK = (short) 0x10;

    /** Mask for the SECURITY INVOKER/DEFINER field */
    private static final short SECURITY_DEFINER_MASK = (short) 0x20;

    private int parameterCount;

    /**
     * Types of the parameters. If there are no parameters
     * then this may be null (or a zero length array).
     */
    private DataTypeDescriptor[] parameterTypes;

    /**
     * Name of each parameter. As of DERBY 10.3, parameter names
     * are optional. If the parameter is unnamed, parameterNames[i]
     * is a string of length 0
     */
    private String[] parameterNames;

    /**
         IN, OUT, INOUT
    */
    private int[] parameterModes;

    private int dynamicResultSets;

    /**
         Return type for functions. Null for procedures.
    */
    private DataTypeDescriptor returnType;

    /**
         Parameter style - always PS_JAVA at the moment.
    */
    private short parameterStyle;

    /**
         This field contains several pieces of information:

         bits 0-3        sqlAllowed = MODIFIES_SQL_DATA, READS_SQL_DATA,CONTAINS_SQL, or NO_SQL

         bit 4               on if function is DETERMINISTIC, off otherwise
         bit 5               on if running with definer's right, off otherwise
    */
    private short   sqlOptions;

    /**
         SQL Specific name (future)
    */
    private String specificName;

    /**
         True if the routine is called on null input.
         (always true for procedures).
    */
    private boolean calledOnNullInput;

    // What type of alias is this: PROCEDURE or FUNCTION?
    private transient char aliasType;

    /**
         Create a RoutineAliasInfo for an internal PROCEDURE.
    */
    public RoutineAliasInfo(String methodName, int parameterCount, String[] parameterNames,
                            DataTypeDescriptor[] parameterTypes, int[] parameterModes, 
                            int dynamicResultSets, short parameterStyle, short sqlAllowed,
                            boolean isDeterministic) {
        this(methodName,
                 parameterCount,
                 parameterNames,
                 parameterTypes,
                 parameterModes,
                 dynamicResultSets,
                 parameterStyle,
                 sqlAllowed,
                 isDeterministic,
                 false /* definersRights */,
                 true,
                 (DataTypeDescriptor)null);
    }

    /**
         Create a RoutineAliasInfo for a PROCEDURE or FUNCTION
    */
    public RoutineAliasInfo(String methodName,
                            int parameterCount,
                            String[] parameterNames,
                            DataTypeDescriptor[] parameterTypes,
                            int[] parameterModes,
                            int dynamicResultSets,
                            short parameterStyle,
                            short sqlAllowed,
                            boolean isDeterministic,
                            boolean definersRights,
                            boolean calledOnNullInput,
                            DataTypeDescriptor returnType) {

        super(methodName);
        this.parameterCount = parameterCount;
        this.parameterNames = parameterNames;
        this.parameterTypes = parameterTypes;
        this.parameterModes = parameterModes;
        this.dynamicResultSets = dynamicResultSets;
        this.parameterStyle = parameterStyle;
        this.sqlOptions = (short)(sqlAllowed & SQL_ALLOWED_MASK);

        if (isDeterministic) { 
            this.sqlOptions = (short)(sqlOptions | DETERMINISTIC_MASK);
        }

        if (definersRights) {
            this.sqlOptions = (short)(sqlOptions | SECURITY_DEFINER_MASK);
        }

        this.calledOnNullInput = calledOnNullInput;
        this.returnType = returnType;

    }

    public int getParameterCount() {
        return parameterCount;
    }

    /**
     * Types of the parameters. If there are no parameters
     * then this may return null (or a zero length array).
     */
    public DataTypeDescriptor[] getParameterTypes() {
        return parameterTypes;
    }

    public int[] getParameterModes() {
        return parameterModes;
    }

    /**
     * Returns an array containing the names of the parameters.
     * As of DERBY 10.3, parameter names are optional (see DERBY-183
     * for more information). If the i-th parameter was unnamed,
     * parameterNames[i] will contain a string of length 0.
     */
    public String[] getParameterNames() {
        return parameterNames;
    }

    public int getMaxDynamicResultSets() {
        return dynamicResultSets;
    }

    public short getParameterStyle() {
        return parameterStyle;
    }

    public short getSQLAllowed() {
        return (short) (sqlOptions & SQL_ALLOWED_MASK);
    }

    public boolean isDeterministic()
    {
        return ( (sqlOptions & DETERMINISTIC_MASK) != 0 );
    }

    public boolean hasDefinersRights()
    {
        return ( (sqlOptions & SECURITY_DEFINER_MASK) != 0 );
    }

    public boolean calledOnNullInput() {
        return calledOnNullInput;
    }

    public DataTypeDescriptor getReturnType() {
        return returnType;
    }

    public boolean isTableFunction() {
        if (returnType == null) { 
            return false;
        }
        else { 
            return returnType.isRowMultiSet(); 
        }
    }

    /**
     * Get this alias info as a string.  NOTE: The "ALIASINFO" column
     * in the SYSALIASES table will return the result of this method
     * on a ResultSet.getString() call.  That said, since the dblook
     * utility uses ResultSet.getString() to retrieve ALIASINFO and
     * to generate the DDL, THIS METHOD MUST RETURN A STRING THAT
     * IS SYNTACTICALLY VALID, or else the DDL generated by dblook
     * will be incorrect.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer(100);
        sb.append(getMethodName());
        sb.append('(');
        for (int i = 0; i < parameterCount; i++) {
            if (i != 0)
                sb.append(',');

            if (returnType == null) {
                // This is a PROCEDURE.  We only want to print the
                // parameter mode (ex. "IN", "OUT", "INOUT") for procedures--
                // we don't do it for functions since use of the "IN" keyword
                // is not part of the FUNCTION syntax.
                sb.append(RoutineAliasInfo.parameterMode(parameterModes[i]));
                sb.append(' ');
            }
            sb.append(parameterNames[i]);
            sb.append(' ');
            sb.append(parameterTypes[i].getSQLstring());
        }
        sb.append(')');

        if (returnType != null) {
            // this a FUNCTION, so syntax requires us to append the return type.
            sb.append(" RETURNS " + returnType.getSQLstring());
        }

        sb.append(" LANGUAGE JAVA PARAMETER STYLE " );

        switch (parameterStyle) {
        case PS_JAVA:        
            sb.append("JAVA "); 
            break;
        case PS_DERBY_JDBC_RESULT_SET:      
            sb.append("DERBY_JDBC_RESULT_SET "); 
            break;
        }
                
        if (isDeterministic()) { 
            sb.append(" DETERMINISTIC "); 
        }

        if (hasDefinersRights()) { 
            sb.append(" EXTERNAL SECURITY DEFINER "); 
        }

        sb.append(RoutineAliasInfo.SQL_CONTROL[getSQLAllowed()]);
        if ((returnType == null) &&
            (dynamicResultSets != 0)) {
            // Only print dynamic result sets if this is a PROCEDURE
            // because it's not valid syntax for FUNCTIONs.
            sb.append(" DYNAMIC RESULT SETS ");
            sb.append(dynamicResultSets);
        }

        if (returnType != null) {
            // this a FUNCTION, so append the syntax telling what to
            // do with a null parameter.
            sb.append(calledOnNullInput ? " CALLED " : " RETURNS NULL ");
            sb.append("ON NULL INPUT");
        }
        
        return sb.toString();
    }

    public static String parameterMode(int parameterMode) {
        switch (parameterMode) {
        case ParameterMetaData.parameterModeIn:
            return "IN";
        case ParameterMetaData.parameterModeOut:
            return "OUT";
        case ParameterMetaData.parameterModeInOut:
            return "INOUT";
        default:
            return "UNKNOWN";
        }
    }
        
}
