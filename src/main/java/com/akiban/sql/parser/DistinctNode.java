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

   Derby - Class org.apache.derby.impl.sql.compile.DistinctNode

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
 * A DistinctNode represents a result set for a disinct operation
 * on a select.  It has the same description as its input result set.
 *
 * For the most part, it simply delegates operations to its childResultSet,
 * which is currently expected to be a ProjectRestrictResultSet generated
 * for a SelectNode.
 *
 * NOTE: A DistinctNode extends FromTable since it can exist in a FromList.
 *
 */
public class DistinctNode extends SingleChildResultSetNode
{
  boolean inSortedOrder;

  /**
   * Initializer for a DistinctNode.
   *
   * @param childResult The child ResultSetNode
   * @param inSortedOrder Whether or not the child ResultSetNode returns its
   *                      output in sorted order.
   * @param tableProperties Properties list associated with the table
   *
   * @exception StandardException Thrown on error
   */
  public void init(Object childResult,
                   Object inSortedOrder,
                   Object tableProperties) 
      throws StandardException {
    super.init(childResult, tableProperties);
    this.inSortedOrder = ((Boolean)inSortedOrder).booleanValue();
  }

  /**
   * Fill this node with a deep copy of the given node.
   */
  public void copyFrom(QueryTreeNode node) throws StandardException {
    super.copyFrom(node);

    DistinctNode other = (DistinctNode)node;
    this.inSortedOrder = other.inSortedOrder;
  }

}
