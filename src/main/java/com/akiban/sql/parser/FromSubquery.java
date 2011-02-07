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

   Derby - Class org.apache.derby.impl.sql.compile.FromSubquery

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
 * A FromSubquery represents a subquery in the FROM list of a DML statement.
 *
 * The current implementation of this class is only
 * sufficient for Insert's need to push a new
 * select on top of the one the user specified,
 * to make the selected structure match that
 * of the insert target table.
 *
 */
public class FromSubquery extends FromTable
{
  ResultSetNode subquery;
  private OrderByList orderByList;
  private ValueNode offset;
  private ValueNode fetchFirst;

  /**
   * Intializer for a table in a FROM list.
   *
   * @param subquery The subquery
   * @param orderByList   ORDER BY list if any, or null
   * @param offset        OFFSET if any, or null
   * @param fetchFirst    FETCH FIRST if any, or null
   * @param correlationName The correlation name
   * @param derivedRCL The derived column list
   * @param tableProperties Properties list associated with the table
   */
  public void init(Object subquery,
                   Object orderByList,
                   Object offset,
                   Object fetchFirst,
                   Object correlationName,
                   Object derivedRCL,
                   Object tableProperties)
  {
    super.init(correlationName, tableProperties);
    this.subquery = (ResultSetNode)subquery;
    this.orderByList = (OrderByList)orderByList;
    this.offset = (ValueNode)offset;
    this.fetchFirst = (ValueNode)fetchFirst;
    resultColumns = (ResultColumnList)derivedRCL;
  }

  /**
   * Prints the sub-nodes of this object.  See QueryTreeNode.java for
   * how tree printing is supposed to work.
   *
   * @param depth The depth of this node in the tree
   */

  public void printSubNodes(int depth) {
    super.printSubNodes(depth);

    if (subquery != null) {
      printLabel(depth, "subquery: ");
      subquery.treePrint(depth + 1);
    }

    if (orderByList != null) {
      printLabel(depth, "orderByList: ");
      orderByList.treePrint(depth + 1);
    }

    if (offset != null) {
      printLabel(depth, "offset: ");
      offset.treePrint(depth + 1);
    }

    if (fetchFirst != null) {
      printLabel(depth, "fetchFirst: ");
      fetchFirst.treePrint(depth + 1);
    }
  }

  /** 
   * Return the "subquery" from this node.
   *
   * @return ResultSetNode The "subquery" from this node.
   */
  public ResultSetNode getSubquery() {
    return subquery;
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
    return super.getFromTableByName(name, schemaName, exactMatch);
  }

  /**
   * Get the exposed name for this table, which is the name that can
   * be used to refer to it in the rest of the query.
   *
   * @return The exposed name for this table.
   */

  public String getExposedName() {
    return correlationName;
  }

}
