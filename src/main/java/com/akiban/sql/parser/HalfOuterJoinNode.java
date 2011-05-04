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

   Derby - Class org.apache.derby.impl.sql.compile.HalfOuterJoinNode

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
 * An HalfOuterJoinNode represents a left or a right outer join result set.
 * Right outer joins are always transformed into left outer joins during
 * preprocessing for simplicity.
 *
 */

public class HalfOuterJoinNode extends JoinNode
{
  private boolean rightOuterJoin;

  /**
   * Initializer for a HalfOuterJoinNode.
   *
   * @param leftResult The ResultSetNode on the left side of this join
   * @param rightResult The ResultSetNode on the right side of this join
   * @param onClause The ON clause
   * @param usingClause The USING clause
   * @param rightOuterJoin Whether or not this node represents a user
   *                       specified right outer join
   * @param tableProperties Properties list associated with the table
   *
   * @exception StandardException Thrown on error
   */

  public void init(Object leftResult,
                   Object rightResult,
                   Object onClause,
                   Object usingClause,
                   Object rightOuterJoin,
                   Object tableProperties)
      throws StandardException {
    super.init(leftResult,
               rightResult,
               onClause,
               usingClause,
               null,
               tableProperties,
               null);
    this.rightOuterJoin = ((Boolean)rightOuterJoin).booleanValue();
  }

  /**
   * Fill this node with a deep copy of the given node.
   */
  public void copyFrom(QueryTreeNode node) throws StandardException {
    super.copyFrom(node);

    HalfOuterJoinNode other = (HalfOuterJoinNode)node;
    this.rightOuterJoin = other.rightOuterJoin;
  }

  public boolean isRightOuterJoin() {
    return rightOuterJoin;
  }

  /**
   * Convert this object to a String.  See comments in QueryTreeNode.java
   * for how this should be done for tree printing.
   *
   * @return This object as a String
   */

  public String toString() {
    return "rightOuterJoin: " + rightOuterJoin + "\n" +
      super.toString();
  }

}
