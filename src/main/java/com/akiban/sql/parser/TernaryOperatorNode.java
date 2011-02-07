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

   Derby - Class org.apache.derby.impl.sql.compile.TernaryOperatorNode

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

/**
 * A TernaryOperatorNode represents a built-in ternary operators.
 * This covers  built-in functions like substr().
 * Java operators are not represented here: the JSQL language allows Java
 * methods to be called from expressions, but not Java operators.
 *
 */

public class TernaryOperatorNode extends ValueNode
{
  String operator;
  String methodName;
  int operatorType;
  ValueNode receiver; 

  ValueNode leftOperand;
  ValueNode rightOperand;

  String resultInterfaceType;
  String receiverInterfaceType;
  String leftInterfaceType;
  String rightInterfaceType;
  int trimType;

  public static final int TRIM = 0;
  public static final int LOCATE = 1;
  public static final int SUBSTRING = 2;
  public static final int LIKE = 3;
  public static final int TIMESTAMPADD = 4;
  public static final int TIMESTAMPDIFF = 5;
  static final String[] TernaryOperators = {
    "trim", "LOCATE", "substring", "like", "TIMESTAMPADD", "TIMESTAMPDIFF"
  };
  static final String[] TernaryMethodNames = {
    "ansiTrim", "locate", "substring", "like", "timestampAdd", "timestampDiff"
  };
  static final String[] TernaryResultType = {
    ValueClassName.StringDataValue, 
    ValueClassName.NumberDataValue,
    ValueClassName.ConcatableDataValue,
    ValueClassName.BooleanDataValue,
    ValueClassName.DateTimeDataValue, 
    ValueClassName.NumberDataValue
  };
  static final String[][] TernaryArgType = {
    { ValueClassName.StringDataValue, ValueClassName.StringDataValue, 
      "java.lang.Integer" },
    { ValueClassName.StringDataValue, ValueClassName.StringDataValue, 
      ValueClassName.NumberDataValue },
    { ValueClassName.ConcatableDataValue, ValueClassName.NumberDataValue, 
      ValueClassName.NumberDataValue },
    { ValueClassName.DataValueDescriptor, ValueClassName.DataValueDescriptor, 
      ValueClassName.DataValueDescriptor },
    { ValueClassName.DateTimeDataValue, "java.lang.Integer", 
      ValueClassName.NumberDataValue }, // time.timestampadd(interval, count)
    { ValueClassName.DateTimeDataValue, "java.lang.Integer", 
      ValueClassName.DateTimeDataValue } // time2.timestampDiff(interval, time1)
  };

  // TODO: enum
  public static final int TRIM_LEADING = 1;
  public static final int TRIM_TRAILING = 2;
  public static final int TRIM_BOTH = 3;

  public static final int YEAR_INTERVAL = 0;
  public static final int QUARTER_INTERVAL = 1;
  public static final int MONTH_INTERVAL = 2;
  public static final int WEEK_INTERVAL = 3;
  public static final int DAY_INTERVAL = 4;
  public static final int HOUR_INTERVAL = 5;
  public static final int MINUTE_INTERVAL = 6;
  public static final int SECOND_INTERVAL = 7;
  public static final int FRAC_SECOND_INTERVAL = 8;

  /**
   * Initializer for a TernaryOperatorNode
   *
   * @param receiver The receiver (eg, string being operated on in substr())
   * @param leftOperand The left operand of the node
   * @param rightOperand The right operand of the node
   * @param operatorType The type of the operand
   */

  public void init(Object receiver,
                   Object leftOperand,
                   Object rightOperand,
                   Object operatorType,
                   Object trimType) {
    this.receiver = (ValueNode)receiver;
    this.leftOperand = (ValueNode)leftOperand;
    this.rightOperand = (ValueNode)rightOperand;
    this.operatorType = ((Integer)operatorType).intValue();
    this.operator = (String)TernaryOperators[this.operatorType];
    this.methodName = (String)TernaryMethodNames[this.operatorType];
    this.resultInterfaceType = (String)TernaryResultType[this.operatorType];
    this.receiverInterfaceType = (String)TernaryArgType[this.operatorType][0];
    this.leftInterfaceType = (String)TernaryArgType[this.operatorType][1];
    this.rightInterfaceType = (String)TernaryArgType[this.operatorType][2];
    if (trimType != null)
      this.trimType = ((Integer)trimType).intValue();
  }

  /**
   * Convert this object to a String.  See comments in QueryTreeNode.java
   * for how this should be done for tree printing.
   *
   * @return This object as a String
   */

  public String toString() {
    return "operator: " + operator + "\n" +
      "methodName: " + methodName + "\n" + 
      "resultInterfaceType: " + resultInterfaceType + "\n" + 
      "receiverInterfaceType: " + receiverInterfaceType + "\n" + 
      "leftInterfaceType: " + leftInterfaceType + "\n" + 
      "rightInterfaceType: " + rightInterfaceType + "\n" + 
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

    if (receiver != null) {
      printLabel(depth, "receiver: ");
      receiver.treePrint(depth + 1);
    }

    if (leftOperand != null) {
      printLabel(depth, "leftOperand: ");
      leftOperand.treePrint(depth + 1);
    }

    if (rightOperand != null) {
      printLabel(depth, "rightOperand: ");
      rightOperand.treePrint(depth + 1);
    }
  }

  /**
   * Set the leftOperand to the specified ValueNode
   *
   * @param newLeftOperand The new leftOperand
   */
  public void setLeftOperand(ValueNode newLeftOperand) {
    leftOperand = newLeftOperand;
  }

  /**
   * Get the leftOperand
   *
   * @return The current leftOperand.
   */
  public ValueNode getLeftOperand() {
    return leftOperand;
  }

  /**
   * Set the rightOperand to the specified ValueNode
   *
   * @param newRightOperand The new rightOperand
   */
  public void setRightOperand(ValueNode newRightOperand) {
    rightOperand = newRightOperand;
  }

  /**
   * Get the rightOperand
   *
   * @return The current rightOperand.
   */
  public ValueNode getRightOperand() {
    return rightOperand;
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

    if (receiver != null) {
      receiver = (ValueNode)receiver.accept(v);
    }

    if (leftOperand != null) {
      leftOperand = (ValueNode)leftOperand.accept(v);
    }

    if (rightOperand != null) {
      rightOperand = (ValueNode)rightOperand.accept(v);
    }
  }
        
  protected boolean isEquivalent(ValueNode o) throws StandardException {
    if (isSameNodeType(o)) {
      TernaryOperatorNode other = (TernaryOperatorNode)o;

      /*
       * SUBSTR function can either have 2 or 3 arguments.  In the 
       * 2-args case, rightOperand will be null and thus needs 
       * additional handling in the equivalence check.
       */
      return (other.methodName.equals(methodName)
              && other.receiver.isEquivalent(receiver)
              && other.leftOperand.isEquivalent(leftOperand)
              && ( (rightOperand == null && other.rightOperand == null) || 
                   (other.rightOperand != null && 
                    other.rightOperand.isEquivalent(rightOperand)) ) );
    }
    return false;
  }

}
