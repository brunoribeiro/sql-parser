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

   Derby - Class org.apache.derby.impl.sql.compile.CreateAliasNode

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
import com.akiban.sql.types.AliasInfo;
import com.akiban.sql.types.DataTypeDescriptor;
import com.akiban.sql.types.RoutineAliasInfo;
import com.akiban.sql.types.SynonymAliasInfo;
import com.akiban.sql.types.UDTAliasInfo;

import java.util.List;

/**
 * A CreateAliasNode represents a CREATE ALIAS statement.
 *
 */

public class CreateAliasNode extends DDLStatementNode
{
  // indexes into routineElements
  public static final int PARAMETER_ARRAY = 0;
  public static final int TABLE_NAME = PARAMETER_ARRAY + 1;
  public static final int DYNAMIC_RESULT_SET_COUNT = TABLE_NAME + 1;
  public static final int LANGUAGE = DYNAMIC_RESULT_SET_COUNT + 1;
  public static final int EXTERNAL_NAME = LANGUAGE + 1;
  public static final int PARAMETER_STYLE = EXTERNAL_NAME + 1;
  public static final int SQL_CONTROL = PARAMETER_STYLE + 1;
  public static final int DETERMINISTIC = SQL_CONTROL + 1;
  public static final int NULL_ON_NULL_INPUT = DETERMINISTIC + 1;
  public static final int RETURN_TYPE = NULL_ON_NULL_INPUT + 1;
  public static final int ROUTINE_SECURITY_DEFINER = RETURN_TYPE + 1;

  // Keep ROUTINE_ELEMENT_COUNT last (determines set cardinality).
  // Note: Remember to also update the map ROUTINE_CLAUSE_NAMES in
  // sqlgrammar.jj when elements are added.
  public static final int ROUTINE_ELEMENT_COUNT = ROUTINE_SECURITY_DEFINER + 1;

  private String javaClassName;
  private String methodName;
  private char aliasType; 
  private boolean delimitedIdentifier;

  private AliasInfo aliasInfo;

  /**
   * Initializer for a CreateAliasNode
   *
   * @param aliasName The name of the alias
   * @param targetObject Target name
   * @param methodName The method name
   * @param aliasType The alias type
   * @param delimitedIdentifier Whether or not to treat the class name
   *        as a delimited identifier if trying to
   *        resolve it as a class alias
   *
   * @exception StandardException Thrown on error
   */
  public void init(Object aliasName,
                   Object targetObject,
                   Object methodName,
                   Object aliasSpecificInfo,
                   Object aliasType,
                   Object delimitedIdentifier) 
      throws StandardException {
    TableName qn = (TableName)aliasName;
    this.aliasType = ((Character)aliasType).charValue();

    initAndCheck(qn);

    switch (this.aliasType) {
    case AliasInfo.ALIAS_TYPE_UDT_AS_CHAR:
      this.javaClassName = (String)targetObject;
      aliasInfo = new UDTAliasInfo();

      implicitCreateSchema = true;
      break;
                
    case AliasInfo.ALIAS_TYPE_PROCEDURE_AS_CHAR:
    case AliasInfo.ALIAS_TYPE_FUNCTION_AS_CHAR:
      {
        this.javaClassName = (String)targetObject;
        this.methodName = (String)methodName;
        this.delimitedIdentifier = ((Boolean)delimitedIdentifier).booleanValue();

        //routineElements contains the description of the procedure.
        // 
        // 0 - Object[] 3 element array for parameters
        // 1 - TableName - specific name
        // 2 - Integer - dynamic result set count
        // 3 - String language (always java) - ignore
        // 4 - String external name (also passed directly to create alias node - ignore
        // 5 - Integer parameter style 
        // 6 - Short - SQL control
        // 7 - Boolean - whether the routine is DETERMINISTIC
        // 8 - Boolean - CALLED ON NULL INPUT (always TRUE for procedures)
        // 9 - DataTypeDescriptor - return type (always NULL for procedures)

        Object[] routineElements = (Object[])aliasSpecificInfo;
        Object[] parameters = (Object[])routineElements[PARAMETER_ARRAY];
        int paramCount = ((List)parameters[0]).size();

        String[] names = null;
        DataTypeDescriptor[] types = null;
        int[] modes = null;

        if (paramCount != 0) {

          names = new String[paramCount];
          ((List<String>)parameters[0]).toArray(names);

          types = new DataTypeDescriptor[paramCount];
          ((List<DataTypeDescriptor>)parameters[1]).toArray(types);

          modes = new int[paramCount];
          for (int i = 0; i < paramCount; i++) {
            int currentMode = ((List<Integer>)parameters[2]).get(i).intValue();
            modes[i] = currentMode;
          }

          if (paramCount > 1) {
            String[] dupNameCheck = new String[paramCount];
            System.arraycopy(names, 0, dupNameCheck, 0, paramCount);
            java.util.Arrays.sort(dupNameCheck);
            for (int dnc = 1; dnc < dupNameCheck.length; dnc++) {
              if (! dupNameCheck[dnc].equals("") && dupNameCheck[dnc].equals(dupNameCheck[dnc - 1]))
                throw new StandardException("Duplicate parameter name");
            }
          }
        }

        Integer drso = (Integer)routineElements[DYNAMIC_RESULT_SET_COUNT];
        int drs = drso == null ? 0 : drso.intValue();

        short sqlAllowed;
        Short sqlAllowedObject = (Short)routineElements[SQL_CONTROL];
        if (sqlAllowedObject != null)
          sqlAllowed = sqlAllowedObject.shortValue();
        else
          sqlAllowed = (this.aliasType == AliasInfo.ALIAS_TYPE_PROCEDURE_AS_CHAR ?
                        RoutineAliasInfo.MODIFIES_SQL_DATA : RoutineAliasInfo.READS_SQL_DATA);

        Boolean isDeterministicO = (Boolean)routineElements[DETERMINISTIC];
        boolean isDeterministic = (isDeterministicO == null) ? false : isDeterministicO.booleanValue();

        Boolean definersRightsO = (Boolean)routineElements[ROUTINE_SECURITY_DEFINER];
        boolean definersRights  =
          (definersRightsO == null) ? false :
          definersRightsO.booleanValue();

        Boolean calledOnNullInputO = (Boolean)routineElements[NULL_ON_NULL_INPUT];
        boolean calledOnNullInput;
        if (calledOnNullInputO == null)
          calledOnNullInput = true;
        else
          calledOnNullInput = calledOnNullInputO.booleanValue();

        DataTypeDescriptor returnType = (DataTypeDescriptor)routineElements[RETURN_TYPE];
        aliasInfo = new RoutineAliasInfo(this.methodName,
                                         paramCount,
                                         names,
                                         types,
                                         modes,
                                         drs,
                                         // parameter style:
                                         ((Short)routineElements[PARAMETER_STYLE]).shortValue(),
                                         sqlAllowed,
                                         isDeterministic,
                                         definersRights,
                                         calledOnNullInput,
                                         returnType);

        implicitCreateSchema = true;
      }
      break;

    case AliasInfo.ALIAS_TYPE_SYNONYM_AS_CHAR:
      String targetSchema = null;
      implicitCreateSchema = true;
      TableName t = (TableName)targetObject;
      if (t.getSchemaName() != null)
        targetSchema = t.getSchemaName();
      aliasInfo = new SynonymAliasInfo(targetSchema, t.getTableName());
      break;

    default:
      assert false : "Unexpected value for aliasType " + aliasType;
    }
  }

  /**
   * Fill this node with a deep copy of the given node.
   */
  public void copyFrom(QueryTreeNode node) throws StandardException {
    super.copyFrom(node);

    CreateAliasNode other = (CreateAliasNode)node;
    this.javaClassName = other.javaClassName;
    this.methodName = other.methodName;
    this.aliasType = other.aliasType; 
    this.delimitedIdentifier = other.delimitedIdentifier;
    this.aliasInfo = other.aliasInfo; // TODO: Clone?
  }

  public String statementToString() {
    switch (this.aliasType) {
    case AliasInfo.ALIAS_TYPE_UDT_AS_CHAR:
      return "CREATE TYPE";
    case AliasInfo.ALIAS_TYPE_PROCEDURE_AS_CHAR:
      return "CREATE PROCEDURE";
    case AliasInfo.ALIAS_TYPE_SYNONYM_AS_CHAR:
      return "CREATE SYNONYM";
    default:
      return "CREATE FUNCTION";
    }
  }

}
