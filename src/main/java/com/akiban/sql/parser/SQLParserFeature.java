/**
 * Copyright © 2012 Akiban Technologies, Inc.  All rights
 * reserved.
 *
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This program may also be available under different license terms.
 * For more information, see www.akiban.com or contact
 * licensing@akiban.com.
 *
 * Contributors:
 * Akiban Technologies, Inc.
 */

/** Features of the parser grammar. 
 * In particular, dialect-specific constructs that can be turned off for use with
 * ordinary databases.
 */

package com.akiban.sql.parser;

public enum SQLParserFeature
{
    GEO_INDEX_DEF_FUNC,
    MYSQL_COLUMN_AS_FUNCS,
    MYSQL_LEFT_RIGHT_FUNC,
    DIV_OPERATOR, // integer division
    GROUPING,
    MYSQL_HINTS,
    MYSQL_INTERVAL,
    UNSIGNED,
    INFIX_MOD,
    INFIX_BIT_OPERATORS,
    INFIX_LOGICAL_OPERATORS,
    DOUBLE_QUOTED_STRING
}
