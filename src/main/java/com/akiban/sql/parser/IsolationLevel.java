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
