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

   Derby - Class org.apache.derby.impl.sql.compile.GroupByNode

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
 * A GroupByNode represents a result set for a grouping operation
 * on a select.  Note that this includes a SELECT with aggregates
 * and no grouping columns (in which case the select list is null)
 * It has the same description as its input result set.
 * <p>
 * For the most part, it simply delegates operations to its bottomPRSet,
 * which is currently expected to be a ProjectRestrictResultSet generated
 * for a SelectNode.
 * <p>
 * NOTE: A GroupByNode extends FromTable since it can exist in a FromList.
 * <p>
 * There is a lot of room for optimizations here: <UL>
 * <LI> agg(distinct x) group by x => agg(x) group by x (for min and max) </LI>
 * <LI> min()/max() use index scans if possible, no sort may 
 *		be needed. </LI>
 * </UL>
 *
 *
 */
public class GroupByNode extends SingleChildResultSetNode
{
  /**
   * The GROUP BY list
   */
  GroupByList groupingList;

  /**
   * The list of all aggregates in the query block
   * that contains this group by.
   */
  List<AggregateNode> aggregateList;
  private ValueNode havingClause;
  private SubqueryList havingSubquerys;

  /**
   * Intializer for a GroupByNode.
   *
   * @param bottomPR The child FromTable
   * @param groupingList The groupingList
   * @param aggregateList The list of aggregates from
   *          the query block.  Since aggregation is done
   *          at the same time as grouping, we need them
   *          here.
   * @param havingClause The having clause.
   * @param havingSubquerys subqueries in the having clause.
   * @param tableProperties Properties list associated with the table
   * @param nestingLevel nestingLevel of this group by node. This is used for 
   *     error checking of group by queries with having clause.
   * @exception StandardException Thrown on error
   */
  public void init(Object bottomPR,
                   Object groupingList,
                   Object aggregateList,
                   Object havingClause,
                   Object havingSubquerys,
                   Object tableProperties,
                   Object nestingLevel)
      throws StandardException {
    super.init(bottomPR, tableProperties);
    setLevel(((Integer)nestingLevel).intValue());
    this.havingClause = (ValueNode)havingClause;
    this.havingSubquerys = (SubqueryList)havingSubquerys;
    this.groupingList = (GroupByList)groupingList;
    this.aggregateList = (List<AggregateNode>)aggregateList;
  }

  /**
   * Convert this object to a String.  See comments in QueryTreeNode.java
   * for how this should be done for tree printing.
   *
   * @return This object as a String
   */

  public String toString() {
    return super.toString();
  }

  /**
   * Prints the sub-nodes of this object.  See QueryTreeNode.java for
   * how tree printing is supposed to work.
   *
   * @param depth The depth of this node in the tree
   */
  public void printSubNodes(int depth) {
    super.printSubNodes(depth);

    printLabel(depth, "aggregateList:\n");

    for (int i=0; i < aggregateList.size(); i++) {
      AggregateNode agg = aggregateList.get(i);
      debugPrint(formatNodeString("[" + i + "]:", depth + 1));
      agg.treePrint(depth + 1);
    }

    if (groupingList != null) {
      printLabel(depth, "groupingList: ");
      groupingList.treePrint(depth + 1);
    }

    if (havingClause != null) {
      printLabel(depth, "havingClause: ");
      havingClause.treePrint(depth + 1);
    }

    if (havingSubquerys != null) {
      printLabel(depth, "havingSubqueries: ");
      havingSubquerys.treePrint(depth + 1);
    }
  }

}
