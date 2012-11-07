
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
