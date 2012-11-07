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
