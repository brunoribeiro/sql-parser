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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import java.io.IOException;
import java.io.StringReader;

public class StringCharStreamTest
{
    // The contract of a CharStream is simple, but prey to off-by-one errors.
    // Simplest test is comparing against existing JavaCC-inspired implementation.

    private CharStream s1, s2;
    private static final String STRING = "abc xyz\n1\t2\t3\r\nxxx   yyy";
    private static final char EOF = (char)0;

    @Before
    public void openStreams() {
        s1 = new UCode_CharStream(new StringReader(STRING), 1, 1);
        s2 = new StringCharStream(STRING);
    }

    @After
    public void closeStreams() {
        s1.Done();
        s2.Done();
    }

    @Test
    public void testRead() {
        while (true) {
            char c = read();
            if (c == EOF) break;
        }
    }

    protected char read() {
        char c1, c2;
        try {
            c1 = s1.readChar();
        }
        catch (IOException ex) {
            c1 = EOF;
        }
        try {
            c2 = s2.readChar();
        }
        catch (IOException ex) {
            c2 = EOF;
        }
        assertEquals("readChar", c1, c2);
        return c1;
    }

    protected void compare() {
        assertEquals("getColumn", s1.getColumn(), s2.getColumn());
        assertEquals("getLine", s1.getLine(), s2.getLine());
        assertEquals("getEndColumn", s1.getEndColumn(), s2.getEndColumn());
        assertEquals("getEndLine", s1.getEndLine(), s2.getEndLine());
        assertEquals("getBeginColumn", s1.getBeginColumn(), s2.getBeginColumn());
        assertEquals("getBeginLine", s1.getBeginLine(), s2.getBeginLine());
        assertEquals("GetImage", s1.GetImage(), s2.GetImage());
        assertEquals("getBeginOffset", s1.getBeginOffset(), s2.getBeginOffset());
        assertEquals("getEndOffset", s1.getEndOffset(), s2.getEndOffset());
        int max = s1.getEndOffset() - s1.getBeginOffset() + 1;
        for (int i = 0; i < max; i++)
            assertEquals("GetSuffix("+i+")", new String(s1.GetSuffix(i)), new String(s2.GetSuffix(i)));
    }

}
