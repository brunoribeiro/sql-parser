
/**
 * Transaction access modes.
 *
 */

package com.akiban.sql.parser;

public enum AccessMode {
    READ_ONLY_ACCESS_MODE("READ ONLY"),
    READ_WRITE_ACCESS_MODE("READ WRITE");

    private String syntax;
    AccessMode(String syntax) {
        this.syntax = syntax;
    }
    
    public String getSyntax() {
        return syntax;
    }
}
