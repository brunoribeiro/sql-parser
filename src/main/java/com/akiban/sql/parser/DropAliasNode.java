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

   Derby - Class org.apache.derby.impl.sql.compile.DropAliasNode

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

/**
 * A DropAliasNode  represents a DROP ALIAS statement.
 *
 */

public class DropAliasNode extends DDLStatementNode
{
  private char aliasType;

  /**
   * Initializer for a DropAliasNode
   *
   * @param dropAliasName The name of the method alias being dropped
   * @param aliasType Alias type
   *
   * @exception StandardException
   */
  public void init(Object dropAliasName, Object aliasType) throws StandardException {
    TableName dropItem = (TableName)dropAliasName;
    initAndCheck(dropItem);
    this.aliasType = ((Character)aliasType).charValue();
  }

  /**
   * Fill this node with a deep copy of the given node.
   */
  public void copyFrom(QueryTreeNode node) throws StandardException {
    super.copyFrom(node);

    DropAliasNode other = (DropAliasNode)node;
    this.aliasType = other.aliasType;
  }

  public char getAliasType() { 
    return aliasType; 
  }

  public String statementToString() {
    return "DROP ".concat(aliasTypeName(aliasType));
  }

  /* returns the alias type name given the alias char type */
  private static String aliasTypeName(char actualType) {
    String typeName = null;
    switch (actualType) {
    case AliasInfo.ALIAS_TYPE_PROCEDURE_AS_CHAR:
      typeName = "PROCEDURE";
      break;
    case AliasInfo.ALIAS_TYPE_FUNCTION_AS_CHAR:
      typeName = "FUNCTION";
      break;
    case AliasInfo.ALIAS_TYPE_SYNONYM_AS_CHAR:
      typeName = "SYNONYM";
      break;
    case AliasInfo.ALIAS_TYPE_UDT_AS_CHAR:
      typeName = "TYPE";
      break;
    }
    return typeName;
  }

}
