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

   Derby - Class org.apache.derby.impl.sql.compile.BitTypeCompiler

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

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.types.DataTypeDescriptor;
import com.akiban.sql.types.TypeId;

/**
 * This class implements TypeCompiler for the SQL BIT datatype.
 *
 */

public class BitTypeCompiler extends TypeCompiler
{
  protected BitTypeCompiler(TypeId typeId) {
    super(typeId);
  }

  /**
   * Tell whether this type (bit) can be converted to the given type.
   *
   * @see TypeCompiler#convertible
   */
  public boolean convertible(TypeId otherType, boolean forDataTypeFunction) {
    if (otherType.isAnsiUDT()) { 
      return false; 
    }
    return (otherType.isBitTypeId() ||
            otherType.isBlobTypeId() ||
            otherType.userType());
  }

  /**
   * Tell whether this type (bit) is compatible with the given type.
   *
   * @param otherType The TypeId of the other type.
   */
  public boolean compatible(TypeId otherType) {
    if (otherType.isBlobTypeId())
      return false;
    return (otherType.isBitTypeId());
  }

  /**
   * @see TypeCompiler#getCorrespondingPrimitiveTypeName
   */
  public String getCorrespondingPrimitiveTypeName() {
    return "byte[]";
  }

  /**
   * Get the method name for getting out the corresponding primitive
   * Java type.
   *
   * @return String The method call name for getting the
   *                corresponding primitive Java type.
   */
  public String getPrimitiveMethodName() {
    return "getBytes";
  }

  /**
   * @see TypeCompiler#getCastToCharWidth
   */
  public int getCastToCharWidth(DataTypeDescriptor dts) {
    return dts.getMaximumWidth();
  }

}
