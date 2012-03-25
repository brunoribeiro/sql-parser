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

/**
 * SQL Parser.
 *
 */

// TODO: The Derby exception handling coordinated localized messages
// and SQLSTATE values, which will be needed, but in the context of
// the new engine.

package com.akiban.sql.parser;

import com.akiban.sql.StandardException;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SQLParser implements SQLParserContext {
    private String sqlText;
    private List<ParameterNode> parameterList;
    private boolean returnParameterFlag;
    private Map printedObjectsMap;
    private int generatedColumnNameIndex;
    
    static final int LARGE_TOKEN_SIZE = 128;

    private CharStream charStream = null;
    private SQLGrammarTokenManager tokenManager = null;
    private SQLGrammar parser = null;

    private int maxStringLiteralLength = 65535;
    /* Identifiers (Constraint, Cursor, Function/Procedure, Index,
     * Trigger, Column, Schema, Savepoint, Table and View names) are
     * limited to 128.
     */ 
    private int maxIdentifierLength = 128;

    // TODO: Needs much more thought.
    private String messageLocale = null;

    // TODO: For now, has most MySQL stuff turned on.
    private Set<SQLParserFeature> features = 
        EnumSet.of(SQLParserFeature.GROUPING,
                   SQLParserFeature.MOD_INFIX, 
                   SQLParserFeature.UNSIGNED,
                   SQLParserFeature.MYSQL_HINTS,
                   SQLParserFeature.MYSQL_INTERVAL);

    NodeFactory nodeFactory;

    /** Make a new parser.
     * Parser can be reused.
     */
    public SQLParser() {
        nodeFactory = new NodeFactoryImpl();
    }

    /** Return the SQL string this parser just parsed. */
    public String getSQLText() {
        return sqlText;
    }

    /** Return the parameters to the parsed statement. */
    public List<ParameterNode> getParameterList() {
        return parameterList;
    }

    /**
     * Looks up an unnamed parameter given its parameter number.
     *
     * @param paramNumber Number of parameter in unnamedparameter list.
     *
     * @return corresponding unnamed parameter.
     *
     */
    public ParameterNode lookupUnnamedParameter(int paramNumber) {
        return parameterList.get(paramNumber);
    }

    /** Normal external parser entry. */
    public StatementNode parseStatement(String sqlText) throws StandardException {
        reinit(sqlText);
        try {
            return parser.parseStatement(sqlText, parameterList);
        }
        catch (ParseException ex) {
            throw new SQLParserException(standardizeEol(ex.getMessage()),
                                         ex, 
                                         tokenErrorPosition(ex.currentToken, sqlText));
        }
        catch (TokenMgrError ex) {
            // Throw away the cached parser.
            parser = null;
            if (ex.errorCode == TokenMgrError.LEXICAL_ERROR)
                throw new SQLParserException(ex.getMessage(),
                                             ex,
                                             lineColumnErrorPosition(ex.errorLine,
                                                                     ex.errorColumn,
                                                                     sqlText));
            else
                throw new StandardException(ex);
        }
    }

    /** Parse multiple statements delimited by semicolons. */
    public List<StatementNode> parseStatements(String sqlText) throws StandardException {
        reinit(sqlText);
        try {
            return parser.parseStatements(sqlText);
        }
        catch (ParseException ex) {
            throw new SQLParserException(standardizeEol(ex.getMessage()),
                                         ex, 
                                         tokenErrorPosition(ex.currentToken, sqlText));
        }
        catch (TokenMgrError ex) {
            // Throw away the cached parser.
            parser = null;
            if (ex.errorCode == TokenMgrError.LEXICAL_ERROR)
                throw new SQLParserException(ex.getMessage(),
                                             ex,
                                             lineColumnErrorPosition(ex.errorLine,
                                                                     ex.errorColumn,
                                                                     sqlText));
            else
                throw new StandardException(ex);
        }
    }

    /** Undo ParseException.initialise()'s eol handling. 
     * Want something platform independent.
     */
    private static String standardizeEol(String msg) {
        String eol = System.getProperty("line.separator", "\n");
        if (eol.equals("\n"))
            return msg;
        else
            return msg.replaceAll(eol, "\n");
    }

    /** Translate position of token into linear position. */
    private static int tokenErrorPosition(Token token, String sql) {
        if (token == null) return 0;
        return lineColumnErrorPosition(token.next.beginLine, token.next.beginColumn, sql);
    }

    /** Translate line position into linear position. */
    private static int lineColumnErrorPosition(int line, int column, String sql) {
        if (line <= 0) return 0;
        int position = 0;
        while (line-- > 1) {
            position = sql.indexOf('\n', position);
            if (position < 0)
                return 0;
            position++;
        }
        position += column;
        return position;
    }

    protected void reinit(String sqlText) throws StandardException {
        this.sqlText = sqlText;
        Reader reader = new StringReader(sqlText);
        if (charStream == null) {
            charStream = new UCode_CharStream(reader, 1, 1, LARGE_TOKEN_SIZE);
        }
        else {
            charStream.ReInit(reader, 1, 1, LARGE_TOKEN_SIZE);
        }
        if (tokenManager == null) {
            tokenManager = new SQLGrammarTokenManager(null, charStream);
        } 
        else {
            tokenManager.ReInit(charStream);
        }
        if (parser == null) {
            parser = new SQLGrammar(tokenManager);
            parser.setParserContext(this);
        }
        else {
            parser.ReInit(tokenManager);
        }
        tokenManager.parser = parser;
        parameterList = new ArrayList<ParameterNode>();
        returnParameterFlag = false;
        printedObjectsMap = null;
        generatedColumnNameIndex = 1;
    }

    /** Get maximum length of a string literal. */
    public int getMaxStringLiteralLength() {
        return maxStringLiteralLength;
    }
    /** Set maximum length of a string literal. */
    public void setMaxStringLiteralLength(int maxLength) {
        maxStringLiteralLength = maxLength;
    }

    /** Check that string literal is not too long. */
    public void checkStringLiteralLengthLimit(String image) throws StandardException {
        if (image.length() > maxStringLiteralLength) {
            throw new StandardException("String literal too long");
        }
    }

    /** Get maximum length of an identifier. */
    public int getMaxIdentifierLength() {
        return maxIdentifierLength;
    }
    /** Set maximum length of an identifier. */
    public void setMaxIdentifierLength(int maxLength) {
        maxIdentifierLength = maxLength;
    }

    /**
     * Check that identifier is not too long.
     */
    public void checkIdentifierLengthLimit(String identifier)
            throws StandardException {
        if (identifier.length() > maxIdentifierLength)
            throw new StandardException("Identifier too long: '" + identifier + "'");
    }

    public void setReturnParameterFlag() {
        returnParameterFlag = true;
    }

    public String getMessageLocale() {
        return messageLocale;
    }
    public void setMessageLocale(String locale) {
        messageLocale = locale;
    }

    /** Get a node factory. */
    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    /** Set the node factory. */
    public void setNodeFactory(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    /**
     * Return a map of AST nodes that have already been printed during a
     * compiler phase, so as to be able to avoid printing a node more than once.
     * @see QueryTreeNode#treePrint(int)
     * @return the map
     */
    public Map getPrintedObjectsMap() {
        if (printedObjectsMap == null)
            printedObjectsMap = new HashMap();
        return printedObjectsMap;
    }

    public String generateColumnName() {
        return "_SQL_COL_" + generatedColumnNameIndex++;
    }
    
    public Set<SQLParserFeature> getFeatures() {
        return features;
    }

    public boolean hasFeature(SQLParserFeature feature) {
        return features.contains(feature);
    }

    public IdentifierCase getIdentifierCase() {
        return IdentifierCase.LOWER;
    }

}
