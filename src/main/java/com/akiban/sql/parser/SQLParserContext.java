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
}
