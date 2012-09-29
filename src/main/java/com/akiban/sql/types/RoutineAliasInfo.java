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
 * @see AliasInfo
 */
public class RoutineAliasInfo extends MethodAliasInfo
{
    public static enum SQLAllowed {
        MODIFIES_SQL_DATA("MODIFIES SQL DATA"),
        READS_SQL_DATA("READS SQL DATA"), 
        CONTAINS_SQL("CONTAINS SQL"), 
        NO_SQL("NO SQL");

        private String sql;

        private SQLAllowed(String sql) {
            this.sql = sql;
        }

        public String getSQL() {
            return sql;
        }
   }

    public static enum ParameterStyle {
        JAVA, DERBY_JDBC_RESULT_SET, AKIBAN_LOADABLE_PLAN, ENVIRONMENT
    }

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
     * ParameterMetaData.parameterModeXxx: IN, OUT, INOUT
     */
    private int[] parameterModes;

    private int dynamicResultSets;

    /**
     * Return type for functions. Null for procedures.
     */
    private DataTypeDescriptor returnType;

    private String language;

    private ParameterStyle parameterStyle;

    private SQLAllowed sqlAllowed;

    private boolean deterministic;

    private boolean definersRights;

    /**
     * SQL Specific name (future)
     */
    private String specificName;

    /**
     * True if the routine is called on null input.
     * (always true for procedures).
     */
    private boolean calledOnNullInput;

    /**
     * Create a RoutineAliasInfo for a PROCEDURE or FUNCTION
     */
    public RoutineAliasInfo(String methodName,
                            int parameterCount,
                            String[] parameterNames,
                            DataTypeDescriptor[] parameterTypes,
                            int[] parameterModes,
                            int dynamicResultSets,
                            String language,
                            ParameterStyle parameterStyle,
                            SQLAllowed sqlAllowed,
                            boolean deterministic,
                            boolean definersRights,
                            boolean calledOnNullInput,
                            DataTypeDescriptor returnType) {

        super(methodName);
        this.parameterCount = parameterCount;
        this.parameterNames = parameterNames;
        this.parameterTypes = parameterTypes;
        this.parameterModes = parameterModes;
        this.dynamicResultSets = dynamicResultSets;
        this.language = language;
        this.parameterStyle = parameterStyle;
        this.sqlAllowed = sqlAllowed;
        this.deterministic = deterministic;
        this.definersRights = definersRights;
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

    public String getLanguage() {
        return language;
    }

    public ParameterStyle getParameterStyle() {
        return parameterStyle;
    }

    public SQLAllowed getSQLAllowed() {
        return sqlAllowed;
    }

    public boolean isDeterministic()
    {
        return deterministic;
    }

    public boolean hasDefinersRights()
    {
        return definersRights;
    }

    public boolean calledOnNullInput() {
        return calledOnNullInput;
    }

    public DataTypeDescriptor getReturnType() {
        return returnType;
    }

    public boolean isFunction() {
        return (returnType != null);
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
     * Get this alias info as a string.  
     * This method must return a string that is syntactically valid.
     */
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append('(');
        for (int i = 0; i < parameterCount; i++) {
            if (i != 0)
                sb.append(", ");

            if (returnType == null) {
                // This is a PROCEDURE.  We only want to print the
                // parameter mode (ex. "IN", "OUT", "INOUT") for procedures--
                // we don't do it for functions since use of the "IN" keyword
                // is not part of the FUNCTION syntax.
                sb.append(parameterMode(parameterModes[i]));
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

        sb.append(" LANGUAGE ");
        sb.append(language);

        sb.append(" PARAMETER STYLE " );
        sb.append(parameterStyle.name());
                
        if (deterministic) { 
            sb.append(" DETERMINISTIC "); 
        }

        if (definersRights) { 
            sb.append(" EXTERNAL SECURITY DEFINER"); 
        }

        sb.append(" ");
        sb.append(sqlAllowed.getSQL());
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
