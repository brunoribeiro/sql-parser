/**
 * Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses.
 */

/** Features of the parser grammar. 
 * In particular, dialect-specific constructs that can be turned off for use with
 * ordinary databases.
 */

package com.akiban.sql.parser;

public enum SQLParserFeature
{
    MOD_INFIX,
    GROUPING,
    UNSIGNED,
    MYSQL_HINTS,
    MYSQL_INTERVAL,
    DOUBLE_QUOTED_STRING
}
