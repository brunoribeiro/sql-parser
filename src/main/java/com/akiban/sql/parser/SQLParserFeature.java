
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
