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

   Derby - Class org.apache.derby.impl.sql.compile.BinaryLogicalOperatorNode

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
import com.akiban.sql.types.ValueClassName;

public abstract class BinaryLogicalOperatorNode extends BinaryOperatorNode
{
  protected boolean shortCircuitValue;

  /**
   * Initializer for a BinaryLogicalOperatorNode
   *
   * @param leftOperand The left operand of the comparison
   * @param rightOperand The right operand of the comparison
   * @param methodName The name of the method to call in the generated
   *        class.  In this case, it's actually an operator name.
   */

  public void init(Object leftOperand,
                   Object rightOperand,
                   Object methodName) {
    /* For logical operators, the operator and method names are the same */
    super.init(leftOperand, rightOperand, methodName, methodName,
               ValueClassName.BooleanDataValue, ValueClassName.BooleanDataValue);
  }

  /**
   * Fill this node with a deep copy of the given node.
   */
  public void copyFrom(QueryTreeNode node) throws StandardException {
    super.copyFrom(node);

    BinaryLogicalOperatorNode other = (BinaryLogicalOperatorNode)node;
    this.shortCircuitValue = other.shortCircuitValue;
  }

}
