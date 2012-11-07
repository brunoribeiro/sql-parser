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

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

import java.util.Map;

public interface SQLParserContext
{
    /** Check that string literal is not too long. */
    public void checkStringLiteralLengthLimit(String image) throws StandardException;

    /** Check that identifier is not too long. */
    public void checkIdentifierLengthLimit(String identifier) throws StandardException;
    
    /** Mark as returning a parameter. */
    public void setReturnParameterFlag();

    /** Mark as requesting locale. */
    public void setMessageLocale(String locale);

    /** Get a node factory. */
    public NodeFactory getNodeFactory();

    /**
     * Return a map of AST nodes that have already been printed during a
     * compiler phase, so as to be able to avoid printing a node more than once.
     * @see QueryTreeNode#treePrint(int)
     * @return the map
     */
    public Map getPrintedObjectsMap();

    /** Is the given feature enabled for this parser? */
    public boolean hasFeature(SQLParserFeature feature);

    enum IdentifierCase { UPPER, LOWER, PRESERVE };

    /** How are unquoted identifiers standardized? **/
    public IdentifierCase getIdentifierCase();
}
