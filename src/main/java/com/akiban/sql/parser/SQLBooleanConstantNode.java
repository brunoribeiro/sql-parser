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

   Derby - Class org.apache.derby.impl.sql.compile.SQLBooleanConstantNode

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
import com.akiban.sql.types.TypeId;

public class SQLBooleanConstantNode extends ConstantNode
{
  /**
   * Initializer for a SQLBooleanConstantNode.
   *
   * @param newValue A String containing the value of the constant: true, false, unknown
   *
   * @exception StandardException
   */

  public void init(Object newValue) throws StandardException {
    String strVal = (String)newValue;
    Boolean val = null;

    if ("true".equalsIgnoreCase(strVal))
      val = Boolean.TRUE;
    else if ("false".equalsIgnoreCase(strVal))
      val = Boolean.FALSE;

    /*
    ** RESOLVE: The length is fixed at 1, even for nulls.
    ** Is that OK?
    */

    /* Fill in the type information in the parent ValueNode */
    super.init(TypeId.BOOLEAN_ID,
               Boolean.TRUE,
               1);

    setValue(val);
  }

}
