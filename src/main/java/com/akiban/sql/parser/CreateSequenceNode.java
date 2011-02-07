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

   Derby - Class org.apache.derby.impl.sql.compile.CreateSequenceNode

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
import com.akiban.sql.types.DataTypeDescriptor;
import com.akiban.sql.types.TypeId;

/**
 * A CreateSequenceNode is the root of a QueryTree that
 * represents a CREATE SEQUENCE statement.
 */

public class CreateSequenceNode extends DDLStatementNode
{
  private TableName sequenceName;
  private DataTypeDescriptor dataType;
  private long initialValue;
  private long stepValue;
  private long maxValue;
  private long minValue;
  private boolean cycle;

  /**
   * Initializer for a CreateSequenceNode
   *
   * @param sequenceName The name of the new sequence
   * @param dataType Exact numeric type of the new sequence
   * @param initialValue Starting value
   * @param stepValue Increment amount
   * @param maxValue Largest value returned by the sequence generator
   * @param minValue Smallest value returned by the sequence generator
   * @param cycle True if the generator should wrap around, false otherwise
   *
   * @throws StandardException on error
   */
  public void init (Object sequenceName,
                    Object dataType,
                    Object initialValue,
                    Object stepValue,
                    Object maxValue,
                    Object minValue,
                    Object cycle) 
      throws StandardException {

    this.sequenceName = (TableName)sequenceName;
    initAndCheck(this.sequenceName);

    if (dataType != null) {
      this.dataType = (DataTypeDescriptor)dataType;
    } 
    else {
      this.dataType = DataTypeDescriptor.INTEGER;
    }

    this.stepValue = stepValue != null ? ((Long)stepValue).longValue() : 1;

    if (this.dataType.getTypeId().equals(TypeId.SMALLINT_ID)) {
      this.minValue = minValue != null ? ((Long)minValue).longValue() : Short.MIN_VALUE;
      this.maxValue = maxValue != null ? ((Long)maxValue).longValue() : Short.MAX_VALUE;
    } 
    else if (this.dataType.getTypeId().equals(TypeId.INTEGER_ID)) {
      this.minValue = minValue != null ? ((Long)minValue).longValue() : Integer.MIN_VALUE;
      this.maxValue = maxValue != null ? ((Long)maxValue).longValue() : Integer.MAX_VALUE;
    }
    else {
      this.minValue = minValue != null ? ((Long)minValue).longValue() : Long.MIN_VALUE;
      this.maxValue = maxValue != null ? ((Long)maxValue).longValue() : Long.MAX_VALUE;
    }

    if (initialValue != null) {
      this.initialValue = ((Long)initialValue).longValue();
    } 
    else {
      if (this.stepValue > 0) {
        this.initialValue = this.minValue;
      } 
      else {
        this.initialValue = this.maxValue;
      }
    }
    this.cycle = cycle != null ? ((Boolean)cycle).booleanValue() : Boolean.FALSE;

  }

  /**
   * Convert this object to a String.  See comments in QueryTreeNode.java
   * for how this should be done for tree printing.
   *
   * @return This object as a String
   */

  public String toString() {
    return super.toString() +
      "sequenceName: " + "\n" + sequenceName + "\n";
  }

  public String statementToString() {
    return "CREATE SEQUENCE";
  }

}
