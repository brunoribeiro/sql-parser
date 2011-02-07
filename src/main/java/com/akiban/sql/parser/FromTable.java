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

   Derby - Class org.apache.derby.impl.sql.compile.FromTable

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
 * A FromTable represents a table in the FROM clause of a DML statement.
 * It can be either a base table, a subquery or a project restrict.
 *
 * @see FromBaseTable
 * @see FromSubquery
 * @see ProjectRestrictNode
 *
 */
abstract class FromTable extends ResultSetNode
{
  Properties tableProperties;
  String correlationName;
  TableName corrTableName;
  int tableNumber;
  /* (Query block) level is 0-based. */
  /* RESOLVE - View resolution will have to update the level within
   * the view tree.
   */
  int level;

  /** the original unbound table name */
  protected TableName origTableName;

  /**
   * Initializer for a table in a FROM list.
   *
   * @param correlationName The correlation name
   * @param tableProperties Properties list associated with the table
   */
  public void init(Object correlationName, Object tableProperties) {
    this.correlationName = (String)correlationName;
    this.tableProperties = (Properties)tableProperties;
  }

  /**
   * Get this table's correlation name, if any.
   */
  public String getCorrelationName() { 
    return correlationName; 
  }

  /**
   * Convert this object to a String.  See comments in QueryTreeNode.java
   * for how this should be done for tree printing.
   *
   * @return This object as a String
   */

  public String toString() {
    return "correlation Name: " + correlationName + "\n" +
      (corrTableName != null ?
       corrTableName.toString() : "null") + "\n" +
      "tableNumber " + tableNumber + "\n" +
      "level " + level + "\n" +
      super.toString();
  }

  /**
   * Return a TableName node representing this FromTable.
   * Expect this to be overridden (and used) by subclasses
   * that may set correlationName to null.
   *
   * @return a TableName node representing this FromTable.
   * @exception StandardException Thrown on error
   */
  public TableName getTableName() throws StandardException {
    if (correlationName == null) return null;

    if (corrTableName == null) {
      corrTableName = makeTableName(null, correlationName);
    }

    return corrTableName;
  }

  /**
   * Set the (query block) level (0-based) for this FromTable.
   *
   * @param level The query block level for this FromTable.
   */
  public void setLevel(int level) {
    this.level = level;
  }

  /**
   * Get the (query block) level (0-based) for this FromTable.
   *
   * @return int The query block level for this FromTable.
   */
  public int getLevel() {
    return level;
  }

  /**
   * Decrement (query block) level (0-based) for this FromTable.
   * This is useful when flattening a subquery.
   *
   * @param decrement The amount to decrement by.
   */
  void decrementLevel(int decrement) {
    assert (level < decrement && level != 0);
    /* NOTE: level doesn't get propagated 
     * to nodes generated after binding.
     */
    if (level > 0) {
      level -= decrement;
    }
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
    // Only FromBaseTables have schema names
    if (schemaName != null) {
      return null;
    }

    if (getExposedName().equals(name)) {
      return this;
    }
    return null;
  }

  public String getExposedName() throws StandardException {
    return null;
  }

  /**
   * Sets the original or unbound table name for this FromTable.  
   * 
   * @param tableName the unbound table name
   *
   */
  public void setOrigTableName(TableName tableName) {
    this.origTableName = tableName;
  }

  /**
   * Gets the original or unbound table name for this FromTable.  
   * The tableName field can be changed due to synonym resolution.
   * Use this method to retrieve the actual unbound tablename.
   * 
   * @return TableName the original or unbound tablename
   *
   */
  public TableName getOrigTableName() {
    return this.origTableName;
  }

}
