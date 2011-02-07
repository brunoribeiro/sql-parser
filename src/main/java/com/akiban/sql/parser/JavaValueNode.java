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

   Derby - Class org.apache.derby.impl.sql.compile.JavaValueNode

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
import com.akiban.sql.types.JSQLType;
import com.akiban.sql.types.TypeId;

/**
 * This abstract node class represents a data value in the Java domain.
 */

// TODO: I think this is too much (or too little).

abstract class JavaValueNode extends QueryTreeNode
{
  private boolean mustCastToPrimitive;

  protected boolean forCallStatement;
  private boolean valueReturnedToSQLDomain;
  private boolean returnValueDiscarded;
  protected JSQLType jsqlType;

  /**
   * Get the resolved data type of this node. May be overridden by descendants.
   */
  public DataTypeDescriptor getDataType() throws StandardException {
    return DataTypeDescriptor.getSQLDataTypeDescriptor(getJavaTypeName());
  }

  public boolean isPrimitiveType() throws StandardException {
    JSQLType myType = getJSQLType();

    if (myType == null) { 
      return false;
    }
    else { 
      return (myType.getCategory() == JSQLType.JAVA_PRIMITIVE); 
    }
  }

  public String getJavaTypeName() throws StandardException {
    JSQLType myType = getJSQLType();

    if (myType == null) { 
      return ""; 
    }

    switch(myType.getCategory()) {
    case JSQLType.JAVA_CLASS: 
      return myType.getJavaClassName();

    case JSQLType.JAVA_PRIMITIVE: 
      return JSQLType.getPrimitiveName(myType.getPrimitiveKind());

    default:
      assert false : "Inappropriate JSQLType: " + myType;
    }

    return "";
  }

  public void setJavaTypeName(String javaTypeName) {
    jsqlType = new JSQLType(javaTypeName);
  }

  public String getPrimitiveTypeName() throws StandardException {
    JSQLType myType = getJSQLType();

    if (myType == null) { 
      return ""; 
    }

    switch(myType.getCategory()) {
    case JSQLType.JAVA_PRIMITIVE: 
      return JSQLType.getPrimitiveName(myType.getPrimitiveKind());

    default:
      assert false : "Inappropriate JSQLType: " + myType;
    }

    return "";
  }

  /**
   * Toggles whether the code generator should add a cast to extract a primitive
   * value from an object.
   *
   * @param booleanValue true if we want the code generator to add a cast
   *                     false otherwise
   */
  public void castToPrimitive(boolean booleanValue) {
    mustCastToPrimitive = booleanValue;
  }

  /**
   * Reports whether the code generator should add a cast to extract a primitive
   * value from an object.
   *
   * @return true if we want the code generator to add a cast
   *         false otherwise
   */
  public boolean mustCastToPrimitive() { 
    return mustCastToPrimitive; 
  }

  /**
   * Get the JSQLType that corresponds to this node. Could be a SQLTYPE,
   * a Java primitive, or a Java class.
   *
   * @return the corresponding JSQLType
   *
   */
  public JSQLType getJSQLType() throws StandardException { 
    return jsqlType; 
  }

  /**
   * Map a JSQLType to a compilation type id.
   *
   * @param jsqlType the universal type to map
   *
   * @return the corresponding compilation type id
   *
   */
  public TypeId mapToTypeID(JSQLType jsqlType) throws StandardException {
    DataTypeDescriptor dts = jsqlType.getSQLType();

    if (dts == null) { 
      return null; 
    }

    return dts.getTypeId();
  }

  /**
   * Mark this node as being for a CALL Statement.
   * (void methods are only okay for CALL Statements)
   */
  public void markForCallStatement() {
    forCallStatement = true;
  }

  /** @see ValueNode#getConstantValueAsObject 
   *
   * @exception StandardException Thrown on error
   */
  Object getConstantValueAsObject() throws StandardException {
    return null;
  }

  /** Inform this node that it returns its value to the SQL domain */
  protected void returnValueToSQLDomain() {
    valueReturnedToSQLDomain = true;
  }

  /** Tell whether this node returns its value to the SQL domain */
  protected boolean valueReturnedToSQLDomain() {
    return valueReturnedToSQLDomain;
  }

  /** Tell this node that nothing is done with the returned value */
  protected void markReturnValueDiscarded() {
    returnValueDiscarded = true;
  }

  /** Tell whether the return value from this node is discarded */
  protected boolean returnValueDiscarded() {
    return returnValueDiscarded;
  }

}
