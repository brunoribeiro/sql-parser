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

   Derby - Class org.apache.derby.impl.sql.compile.FromList

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

import java.util.Properties;

/**
 * A FromList represents the list of tables in a FROM clause in a DML
 * statement.  It extends QueryTreeNodeList.
 *
 */

public class FromList extends QueryTreeNodeList<FromTable>
{
  Properties properties;
  boolean fixedJoinOrder;

  /* Whether or not this FromList is transparent.  A "transparent" FromList
   * is one in which all FromTables are bound based on an outer query's
   * FromList.  This means that the FromTables in the transparent list are
   * allowed to see and reference FromTables in the outer query's list.
   * Or put differently, a FromTable which sits in a transparent FromList
   * does not "see" the transparent FromList when binding; rather, it sees
   * (and can therefore reference) the FromList of an outer query.
   */
  private boolean isTransparent;

  /** Initializer for a FromList */

  public void init(Object optimizeJoinOrder) {
    fixedJoinOrder = ! (((Boolean)optimizeJoinOrder).booleanValue());
    isTransparent = false;
  }

  /**
   * Initializer for a FromList
   *
   * @exception StandardException Thrown on error
   */
  public void init(Object optimizeJoinOrder, Object fromTable)
      throws StandardException {
    init(optimizeJoinOrder);

    addFromTable((FromTable)fromTable);
  }

  /**
   * Add a table to the FROM list.
   *
   * @param fromTable A FromTable to add to the list
   *
   * @exception StandardException Thrown on error
   */

  public void addFromTable(FromTable fromTable) throws StandardException {
    /* Don't worry about checking TableOperatorNodes since
     * they don't have exposed names.  This will potentially
     * allow duplicate exposed names in some degenerate cases,
     * but the binding of the ColumnReferences will catch those
     * cases with a different error.  If the query does not have
     * any ColumnReferences from the duplicate exposed name, the
     * user is executing a really dumb query and we won't throw
     * and exception - consider it an ANSI extension.
     */
    TableName leftTable = null;
    TableName rightTable = null;
    if (!(fromTable instanceof TableOperatorNode)) {
      /* Check for duplicate table name in FROM list */
      leftTable = fromTable.getTableName();
      int size = size();
      for (int index = 0; index < size; index++) {
        if (get(index) instanceof TableOperatorNode) {
          continue;
        }
        else {                    
          rightTable = get(index).getTableName();
        }
        if (leftTable.equals(rightTable)) {
          throw new StandardException("Table duplicated in FROM list: " + 
                                      fromTable.getExposedName());
        }
      }
    }

    add(fromTable);
  }

  /** 
   * Determine whether or not the specified name is an exposed name in
   * the current query block.
   *
   * @param name The specified name to search for as an exposed name.
   * @param schemaName Schema name, if non-null.
   * @param exactMatch Whether or not we need an exact match on specified schema and table
   *                   names or match on table id.
   *
   * @return The FromTable, if any, with the exposed name.
   *
   * @exception StandardException Thrown on error
   */
  protected FromTable getFromTableByName(String name, String schemaName, 
                                         boolean exactMatch)
      throws StandardException {
    boolean found = false;
    FromTable fromTable;
    FromTable result = null;

    int size = size();
    for (int index = 0; index < size; index++) {
      fromTable = get(index);

      result = fromTable.getFromTableByName(name, schemaName, exactMatch);

      if (result != null) {
        return result;
      }
    }
    return result;
  }

  /**
   * Set the (query block) level (0-based) for the FromTables in this
   * FromList.
   *
   * @param level The query block level for this table.
   */
  public void setLevel(int level) {
    int size = size();
    for (int index = 0; index < size; index++) {
      FromTable fromTable = get(index);
      fromTable.setLevel(level);
    }
  }

  /**
   * Set the Properties list for this FromList.
   *
   * @exception StandardException Thrown on error
   */
  public void setProperties(Properties props) throws StandardException {
    properties = props;
  }

}
