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

   Derby - Class org.apache.derby.iapi.sql.StatementType

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

/**
 * Different types of statements
 *
 */
public interface StatementType
{
  // TODO: A bunch of separate enums.

  public static final int UNKNOWN = 0;
  public static final int INSERT = 1;
  public static final int BULK_INSERT_REPLACE = 2;
  public static final int UPDATE = 3;
  public static final int DELETE = 4;
  public static final int ENABLED = 5;
  public static final int DISABLED = 6;

  public static final int DROP_CASCADE = 0;
  public static final int DROP_RESTRICT = 1;
  public static final int DROP_DEFAULT = 2;

  public static final int RA_CASCADE = 0;
  public static final int RA_RESTRICT = 1;
  public static final int RA_NOACTION = 2;  //default value
  public static final int RA_SETNULL = 3;
  public static final int RA_SETDEFAULT = 4;

  public static final int SET_SCHEMA_USER = 1;
  public static final int SET_SCHEMA_DYNAMIC = 2;

  public static final int SET_ROLE_DYNAMIC = 1;

}
