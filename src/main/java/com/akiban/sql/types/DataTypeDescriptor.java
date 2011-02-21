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

   Derby - Class org.apache.derby.iapi.types.DataTypeDescriptor

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

package com.akiban.sql.types;

import com.akiban.sql.StandardException;

import java.sql.Types;

/** 
 * DataTypeDescriptor describes a runtime SQL type.
 * It consists of a catalog type (TypeDescriptor)
 * and runtime attributes. The list of runtime
 * attributes is:
 * <UL>
 * <LI> Collation Derivation
 * </UL>
 * <P>
 * A DataTypeDescriptor is immutable.
 */

// NOTE: The Derby original had two levels of type descriptor, this
// one for in memory and a simpler TypeDescriptor (the "catalog type")
// that was actually stored in the data dictionary. For now, they have
// been combined into this one.

public final class DataTypeDescriptor
{
  public static final int MAXIMUM_WIDTH_UNKNOWN = -1;

  /**
   * Runtime INTEGER type that is nullable.
   */
  public static final DataTypeDescriptor INTEGER =
    new DataTypeDescriptor(TypeId.INTEGER_ID, true);
    
  /**
   * Runtime INTEGER type that is not nullable.
   */
  public static final DataTypeDescriptor INTEGER_NOT_NULL =
    INTEGER.getNullabilityType(false);
    
  /**
   * Runtime SMALLINT type that is nullable.
   */
  public static final DataTypeDescriptor SMALLINT =
    new DataTypeDescriptor(TypeId.SMALLINT_ID, true);
    
  /**
   * Runtime INTEGER type that is not nullable.
   */
  public static final DataTypeDescriptor SMALLINT_NOT_NULL =
    SMALLINT.getNullabilityType(false);
     
  /*
 *** Static creators
  */

  /**
   * Get a descriptor that corresponds to a nullable builtin JDBC type.
   * If a variable length type then the size information will be set 
   * to the maximum possible.
   * 
   * Collation type will be UCS_BASIC and derivation IMPLICIT.
   * 
   * For well known types code may also use the pre-defined
   * runtime types that are fields of this class, such as INTEGER.
   *
   * @param jdbcType The int type of the JDBC type for which to get
   *                 a corresponding SQL DataTypeDescriptor
   *
   * @return A new DataTypeDescriptor that corresponds to the Java type.
   *         A null return value means there is no corresponding SQL type
   */
  public static DataTypeDescriptor getBuiltInDataTypeDescriptor(int jdbcType) {
    return getBuiltInDataTypeDescriptor(jdbcType, true);
  }
    
  /**
   * Get a descriptor that corresponds to a nullable builtin variable
   * length JDBC type.
   *
   * Collation type will be UCS_BASIC and derivation IMPLICIT.
   * 
   * @param jdbcType  The int type of the JDBC type for which to get
   *                      a corresponding SQL DataTypeDescriptor
   *
   * @return  A new DataTypeDescriptor that corresponds to the Java type.
   *          A null return value means there is no corresponding SQL type
   */
  public static DataTypeDescriptor getBuiltInDataTypeDescriptor(int jdbcType, 
                                                                int length) {
    return getBuiltInDataTypeDescriptor(jdbcType, true, length);
  }

  /**
   * Get a descriptor that corresponds to a builtin JDBC type.
   * 
   * For well known types code may also use the pre-defined
   * runtime types that are fields of this class, such as INTEGER.
   * E.g. using DataTypeDescriptor.INTEGER is preferred to
   * DataTypeDescriptor.getBuiltInDataTypeDescriptor(Types.INTEGER, true)
   * (both will return the same immutable object).
   *
   * @param jdbcType The int type of the JDBC type for which to get
   *                 a corresponding SQL DataTypeDescriptor
   * @param isNullable TRUE means it could contain NULL, FALSE means
   *                   it definitely cannot contain NULL.
   *
   * @return A new DataTypeDescriptor that corresponds to the Java type.
   *         A null return value means there is no corresponding SQL type
   */
  public static DataTypeDescriptor getBuiltInDataTypeDescriptor(int jdbcType, 
                                                                boolean isNullable) {
    // Re-use pre-defined types wherever possible.
    switch (jdbcType) {
    case Types.INTEGER:
      return isNullable ? INTEGER : INTEGER_NOT_NULL;
    case Types.SMALLINT:
      return isNullable ? SMALLINT : SMALLINT_NOT_NULL;
    default:
      break;
    }

    TypeId typeId = TypeId.getBuiltInTypeId(jdbcType);
    if (typeId == null) {
      return null;
    }

    return new DataTypeDescriptor(typeId, isNullable);
  }

  /**
   * Get a descriptor that corresponds to a builtin JDBC type.
   * 
   * Collation type will be UCS_BASIC and derivation IMPLICIT.
   *
   * @param jdbcType The int type of the JDBC type for which to get
   *                 a corresponding SQL DataTypeDescriptor
   * @param isNullable TRUE means it could contain NULL, FALSE means
   *                   it definitely cannot contain NULL.
   *
   * @return A new DataTypeDescriptor that corresponds to the Java type.
   *         A null return value means there is no corresponding SQL type
   */
  public static DataTypeDescriptor getBuiltInDataTypeDescriptor(int jdbcType, 
                                                                boolean isNullable, 
                                                                int maxLength) {
    TypeId typeId = TypeId.getBuiltInTypeId(jdbcType);
    if (typeId == null) {
      return null;
    }

    return new DataTypeDescriptor(typeId, isNullable, maxLength);
  }

  /**
   * Get a DataTypeDescriptor that corresponds to a nullable builtin SQL type.
   * 
   * Collation type will be UCS_BASIC and derivation IMPLICIT.
   *
   * @param sqlTypeName The name of the type for which to get
   *                    a corresponding SQL DataTypeDescriptor
   *
   * @return A new DataTypeDescriptor that corresponds to the Java type.
   *         A null return value means there is no corresponding SQL type (only for 'char')
   */
  public static DataTypeDescriptor getBuiltInDataTypeDescriptor(String sqlTypeName) {
    return new DataTypeDescriptor(TypeId.getBuiltInTypeId(sqlTypeName), true);
  }

  /**
   * Get a DataTypeDescriptor that corresponds to a builtin SQL type
   * 
   * Collation type will be UCS_BASIC and derivation IMPLICIT.
   *
   * @param sqlTypeName The name of the type for which to get
   *                    a corresponding SQL DataTypeDescriptor
   *
   * @return A new DataTypeDescriptor that corresponds to the Java type.
   *         A null return value means there is no corresponding SQL type (only for 'char')
   */
  public static DataTypeDescriptor getBuiltInDataTypeDescriptor(String sqlTypeName, 
                                                                int length) {
    return new DataTypeDescriptor(TypeId.getBuiltInTypeId(sqlTypeName), true, length);
  }

  /**
   * Get a DataTypeDescriptor that corresponds to a Java type
   *
   * @param javaTypeName The name of the Java type for which to get
   *                     a corresponding SQL DataTypeDescriptor
   *
   * @return A new DataTypeDescriptor that corresponds to the Java type.
   *         A null return value means there is no corresponding SQL type (only for 'char')
   */
  public static DataTypeDescriptor getSQLDataTypeDescriptor(String javaTypeName) 
      throws StandardException {
    return getSQLDataTypeDescriptor(javaTypeName, true);
  }

  /**
   * Get a DataTypeDescriptor that corresponds to a Java type
   *
   * @param javaTypeName The name of the Java type for which to get
   *                     a corresponding SQL DataTypeDescriptor
   * @param isNullable TRUE means it could contain NULL, FALSE means
   *                   it definitely cannot contain NULL.
   *
   * @return A new DataTypeDescriptor that corresponds to the Java type.
   *         A null return value means there is no corresponding SQL type (only for 'char')
   */
  public static DataTypeDescriptor getSQLDataTypeDescriptor(String javaTypeName, 
                                                            boolean isNullable) 
      throws StandardException {
    TypeId typeId = TypeId.getSQLTypeForJavaType(javaTypeName);
    if (typeId == null) {
      return null;
    }

    return new DataTypeDescriptor(typeId, isNullable);
  }

  /**
   * Get a DataTypeDescriptor that corresponds to a Java type
   *
   * @param javaTypeName The name of the Java type for which to get
   *                     a corresponding SQL DataTypeDescriptor
   * @param precision The number of decimal digits
   * @param scale The number of digits after the decimal point
   * @param isNullable TRUE means it could contain NULL, FALSE means
   *                   it definitely cannot contain NULL.
   * @param maximumWidth The maximum width of a data value
   *                     represented by this type.
   *
   * @return A new DataTypeDescriptor that corresponds to the Java type.
   *         A null return value means there is no corresponding SQL type.
   */
  public static DataTypeDescriptor getSQLDataTypeDescriptor(String javaTypeName, 
                                                            int precision, int scale, 
                                                            boolean isNullable, 
                                                            int maximumWidth)
      throws StandardException {
    TypeId typeId = TypeId.getSQLTypeForJavaType(javaTypeName);
    if (typeId == null) {
      return null;
    }

    return new DataTypeDescriptor(typeId,
                                  precision,
                                  scale,
                                  isNullable,
                                  maximumWidth);
  }
    
  /**
   * Get a catalog type that corresponds to a SQL Row Multiset
   *
   * @param columnNames   Names of the columns in the Row Muliset
   * @param catalogTypes  Types of the columns in the Row Muliset
   *
   * @return A new DataTypeDescriptor describing the SQL Row Multiset
   */
  public static DataTypeDescriptor getRowMultiSet(String[] columnNames,
                                                  DataTypeDescriptor[] columnTypes) {
    return new DataTypeDescriptor(TypeId.getRowMultiSet(columnNames, columnTypes),
                                  true);
  }

  /*
  ** Instance fields & methods
  */
  private TypeId typeId;
  private int precision;
  private int scale;
  private boolean isNullable;
  private int maximumWidth;

  /**
   * Constructor for use with numeric types
   *
   * @param typeId The typeId of the type being described
   * @param precision The number of decimal digits.
   * @param scale The number of digits after the decimal point.
   * @param isNullable TRUE means it could contain NULL, FALSE means
   *                   it definitely cannot contain NULL.
   * @param maximumWidth The maximum number of bytes for this datatype
   */
  public DataTypeDescriptor(TypeId typeId, int precision, int scale,
                            boolean isNullable, int maximumWidth) {
    this.typeId = typeId;
    this.precision = precision;
    this.scale = scale;
    this.isNullable = isNullable;
    this.maximumWidth = maximumWidth;
  }

  /**
   * Constructor for use with non-numeric types
   *
   * @param typeId The typeId of the type being described
   * @param isNullable TRUE means it could contain NULL, FALSE means
   *                   it definitely cannot contain NULL.
   * @param maximumWidth The maximum number of bytes for this datatype
   */
  public DataTypeDescriptor(TypeId typeId, boolean isNullable,
                            int maximumWidth) {
    this.typeId = typeId;
    this.isNullable = isNullable;
    this.maximumWidth = maximumWidth;
  }

  public DataTypeDescriptor(TypeId typeId, boolean isNullable) {

    this.typeId = typeId;
    this.precision = typeId.getMaximumPrecision();
    this.scale = typeId.getMaximumScale();
    this.isNullable = isNullable;
    this.maximumWidth = typeId.getMaximumMaximumWidth();
  }

  private DataTypeDescriptor(DataTypeDescriptor source, boolean isNullable) {
    this.typeId = source.typeId;
    this.precision = source.precision;
    this.scale = source.scale;
    this.isNullable = isNullable;
    this.maximumWidth = source.maximumWidth;
  }

  /**
   * Get maximum width.
   */
  public int getMaximumWidth() {
    return maximumWidth;
  }

  /**
   * Gets the TypeId for the datatype.
   *
   * @return The TypeId for the datatype.
   */
  public TypeId getTypeId() {
    return typeId;
  }

  /**
   * Gets the name of this datatype.
   * 
   *
   *  @return the name of this datatype
   */
  public String getTypeName() {
    return typeId.getSQLTypeName();
  }

  /**
   * Get the jdbc type id for this type.  JDBC type can be
   * found in java.sql.Types. 
   *
   * @return a jdbc type, e.g. java.sql.Types.DECIMAL 
   *
   * @see Types
   */
  public int getJDBCTypeId() {
    return typeId.getJDBCTypeId();
  }

  /**
   * Returns the number of decimal digits for the datatype, if applicable.
   *
   * @return The number of decimal digits for the datatype.  Returns
   *         zero for non-numeric datatypes.
   * @see TypeDescriptor#getPrecision()
   */
  public int getPrecision() {
    return precision;
  }

  /**
   * Returns the number of digits to the right of the decimal for
   * the datatype, if applicable.
   *
   * @return The number of digits to the right of the decimal for
   *         the datatype.  Returns zero for non-numeric datatypes.
   * @see TypeDescriptor#getScale()
   */
  public int getScale() {
    return scale;
  }

  /**
   * Returns TRUE if the datatype can contain NULL, FALSE if not.
   * JDBC supports a return value meaning "nullability unknown" -
   * I assume we will never have columns where the nullability is unknown.
   *
   * @return TRUE if the datatype can contain NULL, FALSE if not.
   */
  public boolean isNullable() {
    return isNullable;
  }
    
  public boolean isRowMultiSet() {
    return typeId.isRowMultiSet();
  }

  /**
   * Return a type descriptor identical to the this type
   * with the exception of its nullability. If the nullablity
   * required matches the nullability of this then this is returned.
   * 
   * @param isNullable True to return a nullable type, false otherwise.
   */
  public DataTypeDescriptor getNullabilityType(boolean isNullable) {
    if (isNullable() == isNullable)
      return this;
        
    return new DataTypeDescriptor(this, isNullable);
  }

  /**
   * Converts this data type descriptor (including length/precision)
   * to a string. E.g.
   *
   *   VARCHAR(30)
   *
   * or
   *
   *   java.util.Hashtable 
   *
   * @return String version of datatype, suitable for running through
   *         the Parser.
   */
  public String getSQLstring() {
    return typeId.toParsableString(this);
  }

  /**
   * Compare JdbcTypeIds to determine if they represent equivalent
   * SQL types. For example Types.NUMERIC and Types.DECIMAL are
   * equivalent
   *
   * @param existingType  JDBC type id of Derby data type
   * @param jdbcTypeId   JDBC type id passed in from application.
   *
   * @return boolean true if types are equivalent, false if not
   */

  public static boolean isJDBCTypeEquivalent(int existingType, int jdbcTypeId) {
    // Any type matches itself.
    if (existingType == jdbcTypeId)
      return true;

    // To a numeric type
    if (isNumericType(existingType)) {
      if (isNumericType(jdbcTypeId))
        return true;

      if (isCharacterType(jdbcTypeId))
        return true;

      return false;
    }

    // To character type.
    if (isCharacterType(existingType)) {

      if (isCharacterType(jdbcTypeId))
        return true;

      if (isNumericType(jdbcTypeId))
        return true;


      switch (jdbcTypeId) {
      case Types.DATE:
      case Types.TIME:
      case Types.TIMESTAMP:
        return true;
      default:
        break;
      }
 
      return false;

    }

    // To binary type
    if (isBinaryType(existingType)) {

      if (isBinaryType(jdbcTypeId))
        return true;

      return false;
    }

    // To DATE, TIME
    if (existingType == Types.DATE || existingType == Types.TIME) {
      if (isCharacterType(jdbcTypeId))
        return true;

      if (jdbcTypeId == Types.TIMESTAMP)
        return true;

      return false;
    }

    // To TIMESTAMP
    if (existingType == Types.TIMESTAMP) {
      if (isCharacterType(jdbcTypeId))
        return true;

      if (jdbcTypeId == Types.DATE)
        return true;

      return false;
    }

    // To CLOB
    if (existingType == Types.CLOB && isCharacterType(jdbcTypeId))
      return true;

    return false;
  }

  public static boolean isNumericType(int jdbcType) {

    switch (jdbcType) {
    case Types.BIT:
    case Types.BOOLEAN:
    case Types.TINYINT:
    case Types.SMALLINT:
    case Types.INTEGER:
    case Types.BIGINT:
    case Types.REAL:
    case Types.FLOAT:
    case Types.DOUBLE:
    case Types.DECIMAL:
    case Types.NUMERIC:
      return true;
    default:
      return false;
    }
  }

  /**
   * Check whether a JDBC type is one of the character types that are
   * compatible with the Java type <code>String</code>.
   *
   * <p><strong>Note:</strong> <code>CLOB</code> is not compatible with
   * <code>String</code>. See tables B-4, B-5 and B-6 in the JDBC 3.0
   * Specification.
   *
   * <p> There are some non-character types that are compatible with
   * <code>String</code> (examples: numeric types, binary types and
   * time-related types), but they are not covered by this method.
   *
   * @param jdbcType a JDBC type
   * @return <code>true</code> iff <code>jdbcType</code> is a character type
   * and compatible with <code>String</code>
   * @see java.sql.Types
   */
  private static boolean isCharacterType(int jdbcType) {

    switch (jdbcType) {
    case Types.CHAR:
    case Types.VARCHAR:
    case Types.LONGVARCHAR:
      return true;
    default:
      return false;
    }
  }

  /**
   * Check whether a JDBC type is compatible with the Java type
   * <code>byte[]</code>.
   *
   * <p><strong>Note:</strong> <code>BLOB</code> is not compatible with
   * <code>byte[]</code>. See tables B-4, B-5 and B-6 in the JDBC 3.0
   * Specification.
   *
   * @param jdbcType a JDBC type
   * @return <code>true</code> iff <code>jdbcType</code> is compatible with
   * <code>byte[]</code>
   * @see java.sql.Types
   */
  private static boolean isBinaryType(int jdbcType) {
    switch (jdbcType) {
    case Types.BINARY:
    case Types.VARBINARY:
    case Types.LONGVARBINARY:
      return true;
    default:
      return false;
    }
  }

  /**
   * Determine if an ASCII stream can be inserted into a column or parameter
   * of type <code>jdbcType</code>.
   *
   * @param jdbcType JDBC type of column or parameter
   * @return <code>true</code> if an ASCII stream can be inserted;
   *         <code>false</code> otherwise
   */
  public static boolean isAsciiStreamAssignable(int jdbcType) {
    return jdbcType == Types.CLOB || isCharacterType(jdbcType);
  }

  /**
   * Determine if a binary stream can be inserted into a column or parameter
   * of type <code>jdbcType</code>.
   *
   * @param jdbcType JDBC type of column or parameter
   * @return <code>true</code> if a binary stream can be inserted;
   *         <code>false</code> otherwise
   */
  public static boolean isBinaryStreamAssignable(int jdbcType) {
    return jdbcType == Types.BLOB || isBinaryType(jdbcType);
  }

  /**
   * Determine if a character stream can be inserted into a column or
   * parameter of type <code>jdbcType</code>.
   *
   * @param jdbcType JDBC type of column or parameter
   * @return <code>true</code> if a character stream can be inserted;
   *         <code>false</code> otherwise
   */
  public static boolean isCharacterStreamAssignable(int jdbcType) {
    // currently, we support the same types for ASCII streams and
    // character streams
    return isAsciiStreamAssignable(jdbcType);
  }

  public String toString() {
    String s = getSQLstring();
    if (!isNullable())
      return s + " NOT NULL";
    return s;
  }

  /**
   * Return the SQL type name and, if applicable, scale/precision/length
   * for this DataTypeDescriptor.  Note that we want the values from *this*
   * object specifically, not the max values defined on this.typeId.
   */
  public String getFullSQLTypeName() {
    StringBuffer sbuf = new StringBuffer(typeId.getSQLTypeName());
    if (typeId.isDecimalTypeId() || typeId.isNumericTypeId()) {
      sbuf.append("(");
      sbuf.append(getPrecision());
      sbuf.append(", ");
      sbuf.append(getScale());
      sbuf.append(")");
    }
    else if (typeId.variableLength()) {
      sbuf.append("(");
      sbuf.append(getMaximumWidth());
      sbuf.append(")");
    }

    return sbuf.toString();
  }

  /**
   * Compute the maximum width (column display width) of a decimal or numeric data value,
   * given its precision and scale.
   *
   * @param precision The precision (number of digits) of the data value.
   * @param scale The number of fractional digits (digits to the right of the decimal point).
   *
   * @return The maximum number of chracters needed to display the value.
   */
  public static int computeMaxWidth (int precision, int scale) {
    // There are 3 possible cases with respect to finding the correct max
    // width for DECIMAL type.
    // 1. If scale = 0, only sign should be added to precision.
    // 2. scale = precision, 3 should be added to precision for sign, decimal and an additional char '0'.
    // 3. precision > scale > 0, 2 should be added to precision for sign and decimal.
    return (scale == 0) ? (precision + 1) : ((scale == precision) ? (precision + 3) : (precision + 2));
  }

}
