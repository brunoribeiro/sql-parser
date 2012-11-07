
package com.akiban.sql.parser;

public enum ExistenceCheck
{
    NO_CONDITION,    // [if [not] exists] statement is not specified
    IF_EXISTS,      // [if exists] is specified
    IF_NOT_EXISTS  // [if not exists] is specified
}
