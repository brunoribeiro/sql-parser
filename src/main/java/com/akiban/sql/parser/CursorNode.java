/* Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

/* The original from which this derives bore the following: */

/*

   Derby - Class org.apache.derby.impl.sql.compile.CursorNode

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

import java.util.List;

/**
 * A CursorNode represents a result set that can be returned to a client.
 * A cursor can be a named cursor created by the DECLARE CURSOR statement,
 * or it can be an unnamed cursor associated with a SELECT statement (more
 * precisely, a table expression that returns rows to the client).  In the
 * latter case, the cursor does not have a name.
 *
 */

public class CursorNode extends DMLStatementNode
{
  public final static int UNSPECIFIED = 0;
  public final static int READ_ONLY = 1;
  public final static int UPDATE = 2;

  private String name;
  private OrderByList orderByList;
  private ValueNode offset;     // <result offset clause> value
  private ValueNode fetchFirst; // <fetch first clause> value
  private String statementType;
  private int updateMode;
  private IsolationLevel scanIsolationLevel = IsolationLevel.UNSPECIFIED_ISOLATION_LEVEL;

  /**
   ** There can only be a list of updatable columns when FOR UPDATE
   ** is specified as part of the cursor specification.
   */
  private List<String> updatableColumns;

  /**
   * Initializer for a CursorNode
   *
   * @param statementType Type of statement (SELECT, UPDATE, INSERT)
   * @param resultSet A ResultSetNode specifying the result set for
   *                  the cursor
   * @param name The name of the cursor, null if no name
   * @param orderByList The order by list for the cursor, null if no
   *                    order by list
   * @param offset The value of a <result offset clause> if present
   * @param fetchFirst The value of a <fetch first clause> if present
   * @param updateMode The user-specified update mode for the cursor,
   *                   for example, CursorNode.READ_ONLY
   * @param updatableColumns The list of updatable columns specified by
   *                         the user in the FOR UPDATE clause, null if no
   *                         updatable columns specified.  May only be
   *                         provided if the updateMode parameter is
   *                         CursorNode.UPDATE.
   */

  public void init(Object statementType,
                   Object resultSet,
                   Object name,
                   Object orderByList,
                   Object offset,
                   Object fetchFirst,
                   Object updateMode,
                   Object updatableColumns) {
    init(resultSet);
    this.name = (String)name;
    this.statementType = (String)statementType;
    this.orderByList = (OrderByList)orderByList;
    this.offset = (ValueNode)offset;
    this.fetchFirst = (ValueNode)fetchFirst;
    this.updateMode = ((Integer)updateMode).intValue();
    this.updatableColumns = (List<String>)updatableColumns;
  }

  public void setScanIsolationLevel(IsolationLevel isolationLevel) {
    this.scanIsolationLevel = isolationLevel;
  }

  /**
   * Convert this object to a String.  See comments in QueryTreeNode.java
   * for how this should be done for tree printing.
   *
   * @return This object as a String
   */

  public String toString() {
    return "name: " + name + "\n" +
      "updateMode: " + updateModeString(updateMode) + "\n" +
      super.toString();
  }

  public String statementToString() {
    return statementType;
  }

  /**
   * Support routine for translating an updateMode identifier to a String
   *
   * @param updateMode An updateMode identifier
   *
   * @return A String representing the update mode.
   */

  private static String updateModeString(int updateMode) {
    switch (updateMode) {
    case UNSPECIFIED:
      return "UNSPECIFIED (" + UNSPECIFIED + ")";

    case READ_ONLY:
      return "READ_ONLY (" + READ_ONLY + ")";

    case UPDATE:
      return "UPDATE (" + UPDATE + ")";

    default:
      return "UNKNOWN VALUE (" + updateMode + ")";
    }
  }

  /**
   * Prints the sub-nodes of this object.  See QueryTreeNode.java for
   * how tree printing is supposed to work.
   *
   * @param depth The depth of this node in the tree
   */

  public void printSubNodes(int depth) {
    super.printSubNodes(depth);

    if (orderByList != null) {
      printLabel(depth, "orderByList: "  + depth);
      orderByList.treePrint(depth + 1);
    }
  }

  public int getUpdateMode() {
    return updateMode;
  }

  /**
   * Return String[] of names from the FOR UPDATE OF List
   *
   * @return String[] of names from the FOR UPDATE OF list.
   */
  private String[] getUpdatableColumns() {
    return (updatableColumns == null) ?
      (String[])null :
      getUpdateColumnNames();
  }

  /**
   * Get an array of strings for each updatable column
   * in this list.
   *
   * @return an array of strings
   */
  private String[] getUpdateColumnNames() {
    int size = updatableColumns.size();
    if (size == 0) {
      return (String[])null;
    }

    String[] names = new String[size];
    updatableColumns.toArray(names);
    return names;
  }

  public String getXML() {
    return null;
  }

}
