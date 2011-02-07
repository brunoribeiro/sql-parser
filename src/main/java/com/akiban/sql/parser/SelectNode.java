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

   Derby - Class org.apache.derby.impl.sql.compile.SelectNode

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
 * A SelectNode represents the result set for any of the basic DML
 * operations: SELECT, INSERT, UPDATE, and DELETE.  (A RowResultSetNode
 * will be used for an INSERT with a VALUES clause.)  For INSERT - SELECT,
 * any of the fields in a SelectNode can be used (the SelectNode represents
 * the SELECT statement in the INSERT - SELECT).  For UPDATE and
 * DELETE, there will be one table in the fromList, and the groupByList
 * fields will be null. For both INSERT and UPDATE,
 * the resultColumns in the selectList will contain the names of the columns
 * being inserted into or updated.
 *
 */

public class SelectNode extends ResultSetNode
{
  /**
   * List of tables in the FROM clause of this SELECT
   */
  FromList fromList;

  /**
   * The ValueNode for the WHERE clause must represent a boolean
   * expression.  The binding phase will enforce this - the parser
   * does not have enough information to enforce it in all cases
   * (for example, user methods that return boolean).
   */
  ValueNode whereClause;

  /**
   * List of result columns in GROUP BY clause
   */
  GroupByList groupByList;

  /**
   * List of windows.
   */
  WindowList windows;

  /* List of columns in ORDER BY list */
  OrderByList orderByList;

  private boolean isDistinct;

  private boolean orderByAndDistinctMerged;

  ValueNode havingClause;

  private int nestingLevel;
  public void init(Object selectList,
                   Object aggregateList,
                   Object fromList,
                   Object whereClause,
                   Object groupByList,
                   Object havingClause,
                   Object windowDefinitionList)
      throws StandardException {
    /* RESOLVE - remove aggregateList from constructor.
     * Consider adding selectAggregates and whereAggregates 
     */
    resultColumns = (ResultColumnList)selectList;
    if (resultColumns != null)
      resultColumns.markInitialSize();
    this.fromList = (FromList)fromList;
    this.whereClause = (ValueNode)whereClause;
    this.groupByList = (GroupByList)groupByList;
    this.havingClause = (ValueNode)havingClause;

    // This initially represents an explicit <window definition list>, as
    // opposed to <in-line window specifications>, see 2003, 6.10 and 6.11.
    // <in-line window specifications> are added later, see right below for
    // in-line window specifications used in window functions in the SELECT
    // column list and in genProjectRestrict for such window specifications
    // used in window functions in ORDER BY.
    this.windows = (WindowList)windowDefinitionList;

    // TODO: Walking to find window and subqueries in WHERE.
  }

  /**
   * Convert this object to a String.  See comments in QueryTreeNode.java
   * for how this should be done for tree printing.
   *
   * @return This object as a String
   */

  public String toString() {
    return "isDistinct: "+ isDistinct + "\n"+
      super.toString();
  }

  public String statementToString() {
    return "SELECT";
  }

  public void makeDistinct() {
    isDistinct = true;
  }

  public void clearDistinct() {
    isDistinct = false;
  }

  boolean hasDistinct() {
    return isDistinct;
  }

  /**
   * Prints the sub-nodes of this object.  See QueryTreeNode.java for
   * how tree printing is supposed to work.
   *
   * @param depth The depth of this node in the tree
   */

  public void printSubNodes(int depth) {
    super.printSubNodes(depth);

    printLabel(depth, "fromList: ");

    if (fromList != null) {
      fromList.treePrint(depth + 1);
    }

    if (whereClause != null) {
      printLabel(depth, "whereClause: ");
      whereClause.treePrint(depth + 1);
    }

    if (groupByList != null) {
      printLabel(depth, "groupByList:");
      groupByList.treePrint(depth + 1);
    }

    if (havingClause != null) {
      printLabel(depth, "havingClause:");
      havingClause.treePrint(depth + 1);
    }

    if (orderByList != null) {
      printLabel(depth, "orderByList:");
      orderByList.treePrint(depth + 1);
    }

    if (windows != null) {
      printLabel(depth, "windows: ");
      windows.treePrint(depth + 1);
    }
  }

  /**
   * Return the fromList for this SelectNode.
   *
   * @return FromList The fromList for this SelectNode.
   */
  public FromList getFromList() {
    return fromList;
  }

  /**
   * Return the whereClause for this SelectNode.
   *
   * @return ValueNode The whereClause for this SelectNode.
   */
  public ValueNode getWhereClause() {
    return whereClause;
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
    return fromList.getFromTableByName(name, schemaName, exactMatch);
  }

  /**
   * Accept the visitor for all visitable children of this node.
   * 
   * @param v the visitor
   *
   * @exception StandardException on error
   */
  void acceptChildren(Visitor v) throws StandardException {
    super.acceptChildren(v);

    if (fromList != null) {
      fromList = (FromList)fromList.accept(v);
    }

    if (whereClause != null) {
      whereClause = (ValueNode)whereClause.accept(v);
    }

    if (havingClause != null) {
      havingClause = (ValueNode)havingClause.accept(v);
    }
  }

  /**
   * Used by SubqueryNode to avoid flattening of a subquery if a window is
   * defined on it. Note that any inline window definitions should have been
   * collected from both the selectList and orderByList at the time this
   * method is called, so the windows list is complete. This is true after
   * preprocess is completed.
   *
   * @return true if this select node has any windows on it
   */
  public boolean hasWindows() {
    return windows != null;
  }

}
