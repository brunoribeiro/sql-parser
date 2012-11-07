
/**
 * Isolation levels.
 */

package com.akiban.sql.parser;

public enum IsolationLevel {
    UNSPECIFIED_ISOLATION_LEVEL("UNSPECIFIED"),
    READ_UNCOMMITTED_ISOLATION_LEVEL("READ UNCOMMITTED"),
    READ_COMMITTED_ISOLATION_LEVEL("READ COMMITTED"),
    REPEATABLE_READ_ISOLATION_LEVEL("REPEATABLE READ"),
    SERIALIZABLE_ISOLATION_LEVEL("SERIALIZABLE");

    private String syntax;
    IsolationLevel(String syntax) {
        this.syntax = syntax;
    }
    
    public String getSyntax() {
        return syntax;
    }
}
