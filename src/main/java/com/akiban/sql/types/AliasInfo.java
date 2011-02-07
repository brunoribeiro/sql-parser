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

   Derby - Class org.apache.derby.catalog.AliasInfo

   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
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

/**
 *
 * An interface for describing an alias in Derby systems.
 * 
 * In a Derby system, an alias can be one of the following:
 * <ul>
 * <li>method alias
 * <li>UDT alias
 * <li>class alias
 * <li>synonym
 * <li>user-defined aggregate
 * </ul>
 *
 */
public interface AliasInfo
{
  // TODO: Just make an enum and have done with it.

  public static final char ALIAS_TYPE_UDT_AS_CHAR = 'A';
  public static final char ALIAS_TYPE_PROCEDURE_AS_CHAR = 'P';
  public static final char ALIAS_TYPE_FUNCTION_AS_CHAR = 'F';
  public static final char ALIAS_TYPE_SYNONYM_AS_CHAR = 'S';

  /**
   * Get the name of the static method that the alias 
   * represents at the source database.  (Only meaningful for
   * method aliases )
   *
   * @return The name of the static method that the alias 
   * represents at the source database.
   */
  public String getMethodName();

  /**
   * Return true if this alias is a Table Function.
   */
  public boolean isTableFunction();

}
