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

   Derby - Class org.apache.derby.impl.sql.compile.TimestampTypeCompiler

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

import java.sql.Types;

public class TimestampTypeCompiler extends TypeCompiler
{
  protected TimestampTypeCompiler(TypeId typeId) {
    super(typeId);
  }

  /**
   * User types are convertible to other user types only if
   * (for now) they are the same type and are being used to
   * implement some JDBC type.  This is sufficient for
   * date/time types; it may be generalized later for e.g.
   * comparison of any user type with one of its subtypes.
   *
   * @see TypeCompiler#convertible 
   *
   */
  public boolean convertible(TypeId otherType, boolean forDataTypeFunction) {
    if (otherType.isStringTypeId()&& 
        (!otherType.isLongConcatableTypeId())) {
      return true;
    }

    int otherJDBCTypeId = otherType.getJDBCTypeId();

    /*
    ** At this point, we have only date/time.  If
    ** same type, convert always ok.
    */
    if (otherJDBCTypeId == Types.TIMESTAMP) {
      return true;
    }

    /*
    ** Otherwise, we can convert timestamp to
    ** date or time only.
    */
    return ((otherJDBCTypeId == Types.DATE) ||
            (otherJDBCTypeId == Types.TIME));
  }

  /**
   * Tell whether this type (timestamp) is compatible with the given type.
   *
   * @param otherType The TypeId of the other type.
   */
  public boolean compatible(TypeId otherType) {
    if (otherType.isStringTypeId() &&
        (!otherType.isLongConcatableTypeId())) {
      return true;
    }
    /*
    ** Both are timestamp datatypes and hence compatible.
    */
    return (getStoredFormatIdFromTypeId() == otherType.getTypeFormatId());
  }
			
  /**
   * @see TypeCompiler#getCorrespondingPrimitiveTypeName
   */

  public String getCorrespondingPrimitiveTypeName() {
    return "java.sql.Timestamp";
  }

  /**
   * Get the method name for getting out the corresponding primitive
   * Java type.
   *
   * @return String The method call name for getting the
   *                corresponding primitive Java type.
   */
  public String getPrimitiveMethodName() {
    return "getTimestamp";
  }

  /**
   * @see TypeCompiler#getCastToCharWidth
   */
  public int getCastToCharWidth(DataTypeDescriptor dts) {
    return 26; // DATE TIME.milliseconds (extra few for good measure)
  }

}
