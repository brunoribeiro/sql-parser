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

   Derby - Class org.apache.derby.impl.sql.compile.ColumnReference

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

/**
 * A ColumnReference represents a column in the query tree.  The parser generates a
 * ColumnReference for each column reference.  A column refercence could be a column in
 * a base table, a column in a view (which could expand into a complex
 * expression), or a column in a subquery in the FROM clause.
 *
 */

public class ColumnReference extends ValueNode
{
  String columnName;

  /*
  ** This is the user-specified table name.  It will be null if the
  ** user specifies a column without a table name.  Leave it null even
  ** when the column is bound as it is only used in binding.
  */
  TableName tableName;

  /**
   * The FromTable this column reference is bound to.
   */
  private int tableNumber;

  /**
   * The column number in the underlying FromTable.
   */
  private int columnNumber;

  /**
   * Initializer.
   * This one is called by the parser where we could
   * be dealing with delimited identifiers.
   *
   * @param columnName The name of the column being referenced
   * @param tableName The qualification for the column
   * @param tokBeginOffset begin position of token for the column name 
   *        identifier from parser.
   * @param tokEndOffsetend position of token for the column name 
   *        identifier from parser.
   */

  public void init(Object columnName, 
                   Object tableName,
                   Object tokBeginOffset,
                   Object tokEndOffset) {
    this.columnName = (String)columnName;
    this.tableName = (TableName)tableName;
    this.setBeginOffset(((Integer)tokBeginOffset).intValue());
    this.setEndOffset(((Integer)tokEndOffset).intValue());
    tableNumber = -1;
  }

  /**
   * Initializer.
   *
   * @param columnName The name of the column being referenced
   * @param tableName The qualification for the column
   */

  public void init(Object columnName, Object tableName) {
    this.columnName = (String)columnName;
    this.tableName = (TableName)tableName;
    tableNumber = -1;
  }

  /**
   * Convert this object to a String.  See comments in QueryTreeNode.java
   * for how this should be done for tree printing.
   *
   * @return This object as a String
   */

  public String toString() {
    return "columnName: " + columnName + "\n" +
      "tableNumber: " + tableNumber + "\n" +
      "columnNumber: " + columnNumber + "\n" +
      "tableName: " + ( ( tableName != null) ?
                        tableName.toString() :
                        "null") + "\n" +
      super.toString();
  }

  /**
   * Prints the sub-nodes of this object.  See QueryTreeNode.java for
   * how tree printing is supposed to work.
   *
   * @param depth The depth of this node in the tree
   */

  public void printSubNodes(int depth) {
    super.printSubNodes(depth);
  }

  /**
   * Get the column name for purposes of error
   * messages or debugging. This returns the column
   * name as used in the SQL statement. Thus if it was qualified
   * with a table, alias name that will be included.
   *
   * @return The  column name in the form [[schema.]table.]column
   */

  public String getSQLColumnName() {
    if (tableName == null)
      return columnName;

    return tableName.toString() + "." + columnName;
  }

  /**
   * Get the name of this column
   *
   * @return The name of this column
   */

  public String getColumnName() {
    return columnName;
  }

  /**
   * Get the table number for this ColumnReference.
   *
   * @return int The table number for this ColumnReference
   */

  public int getTableNumber() {
    return tableNumber;
  }

  /**
   * Set this ColumnReference to refer to the given table number.
   *
   * @param tableNumber The table number this ColumnReference will refer to
   */

  public void setTableNumber(int tableNumber)
  {
    assert tableNumber != -1;
    this.tableNumber = tableNumber;
  }

  /**
   * Get the user-supplied table name of this column.  This will be null
   * if the user did not supply a name (for example, select a from t).
   * The method will return B for this example, select b.a from t as b
   * The method will return T for this example, select t.a from t
   *
   * @return The user-supplied name of this column.  Null if no user-
   *         supplied name.
   */

  public String getTableName() {
    return ((tableName != null) ? tableName.getTableName() : null);
  }

  /**
     Return the table name as the node it is.
     @return the column's table name.
  */
  public TableName getTableNameNode() {
    return tableName;
  }

  public void setTableNameNode(TableName tableName) {
    this.tableName = tableName;
  }

  /**
   * Get the column number for this ColumnReference.
   *
   * @return int The column number for this ColumnReference
   */

  public int getColumnNumber() {
    return columnNumber;
  }

  /**
   * Set the column number for this ColumnReference.  This is
   * used when scoping predicates for pushdown.
   *
   * @param colNum The new column number.
   */

  public void setColumnNumber(int colNum) {
    this.columnNumber = colNum;
  }

  /**
   * Get the user-supplied schema name of this column.  This will be null
   * if the user did not supply a name (for example, select t.a from t).
   * Another example for null return value (for example, select b.a from t as b).
   * But for following query select app.t.a from t, this will return APP
   * Code generation of aggregate functions relies on this method
   *
   * @return The user-supplied schema name of this column.  Null if no user-
   *         supplied name.
   */

  public String getSchemaName() {
    return ((tableName != null) ? tableName.getSchemaName() : null);
  }

  protected boolean isEquivalent(ValueNode o) throws StandardException {
    if (!isSameNodeType(o)) {
      return false;
    }
    ColumnReference other = (ColumnReference)o;
    return (tableNumber == other.tableNumber 
            && columnName.equals(other.getColumnName()));
  }

}
