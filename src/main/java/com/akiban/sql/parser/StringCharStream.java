/**
 * END USER LICENSE AGREEMENT (“EULA”)
 *
 * READ THIS AGREEMENT CAREFULLY (date: 9/13/2011):
 * http://www.akiban.com/licensing/20110913
 *
 * BY INSTALLING OR USING ALL OR ANY PORTION OF THE SOFTWARE, YOU ARE ACCEPTING
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. YOU AGREE THAT THIS
 * AGREEMENT IS ENFORCEABLE LIKE ANY WRITTEN AGREEMENT SIGNED BY YOU.
 *
 * IF YOU HAVE PAID A LICENSE FEE FOR USE OF THE SOFTWARE AND DO NOT AGREE TO
 * THESE TERMS, YOU MAY RETURN THE SOFTWARE FOR A FULL REFUND PROVIDED YOU (A) DO
 * NOT USE THE SOFTWARE AND (B) RETURN THE SOFTWARE WITHIN THIRTY (30) DAYS OF
 * YOUR INITIAL PURCHASE.
 *
 * IF YOU WISH TO USE THE SOFTWARE AS AN EMPLOYEE, CONTRACTOR, OR AGENT OF A
 * CORPORATION, PARTNERSHIP OR SIMILAR ENTITY, THEN YOU MUST BE AUTHORIZED TO SIGN
 * FOR AND BIND THE ENTITY IN ORDER TO ACCEPT THE TERMS OF THIS AGREEMENT. THE
 * LICENSES GRANTED UNDER THIS AGREEMENT ARE EXPRESSLY CONDITIONED UPON ACCEPTANCE
 * BY SUCH AUTHORIZED PERSONNEL.
 *
 * IF YOU HAVE ENTERED INTO A SEPARATE WRITTEN LICENSE AGREEMENT WITH AKIBAN FOR
 * USE OF THE SOFTWARE, THE TERMS AND CONDITIONS OF SUCH OTHER AGREEMENT SHALL
 * PREVAIL OVER ANY CONFLICTING TERMS OR CONDITIONS IN THIS AGREEMENT.
 */

package com.akiban.sql.parser;

import java.io.EOFException;
import java.io.IOException;

/**
 * {@link CharStream} that simply reads from a string.
 */
public class StringCharStream implements CharStream
{
    private static final IOException EOF = new EOFException();

    private String string;
    private int beginOffset, endOffset; // 0-based.
    private int beginLine, beginColumn, endLine, endColumn; // 1-based.
    
    public StringCharStream(String string) {
        init(string);
    }

    public void ReInit(String string) {
        init(string);
    }

    private void init(String string) {
        this.string = string;
        beginOffset = endOffset = 0;
        beginLine = beginColumn = endLine = endColumn = 1;
    }
    
    @Override
    public char BeginToken() throws java.io.IOException {
        beginOffset = endOffset;
        beginLine = endLine;
        beginColumn = endColumn;
        return readChar();
    }

    @Override
    public char readChar() throws java.io.IOException {
        if (endOffset >= string.length())
            throw EOF;

        return advance();
    }

    @Override
    public void backup(int amount) {
        int target = endOffset - amount;
        assert (target >= beginOffset);
        endOffset = beginOffset;
        endLine = beginLine;
        endColumn = beginColumn;
        while (endOffset < target)
            advance();          // Adjusting line / column.
    }

    private char advance() {
        char ch = string.charAt(endOffset++);
        switch (ch) {
        case '\r':
            if ((endOffset < string.length()) &&
                (string.charAt(endOffset) == '\n'))
                break;
            /* else falls through (bare CR) */
        case '\n':
            endLine++;
            endColumn = 1;
            break;
        case '\t':
            endColumn--;
            endColumn += (8 - (endColumn & 7));
            break;
        default:
            endColumn++;
            break;
        }
        return ch;
    }

    @Override
    public int getBeginOffset() {
        return beginOffset;
    }
    @Override
    public int getEndOffset() {
        return endOffset - 1;   // Want inclusive.
    }

    @Override
    public int getBeginLine() {
        return beginLine;
    }
    @Override
    public int getBeginColumn() {
        return beginColumn;
    }

    @Override
    public int getEndLine() {
        return endLine;
    }
    @Override
    public int getEndColumn() {
        return endColumn - 1;   // Want inclusive.
    }

    @Override
    public int getLine() {
        return getEndLine();
    }
    @Override
    public int getColumn() {
        return getEndColumn();
    }

    @Override
    public String GetImage() {
        return string.substring(beginOffset, endOffset);
    }

    @Override
    public char[] GetSuffix(int len) {
        char[] result = new char[len];
        string.getChars(endOffset - len, endOffset, result, 0);
        return result;
    }

    @Override
    public void Done() {
    }

}
