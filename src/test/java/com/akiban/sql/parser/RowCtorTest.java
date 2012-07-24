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

import com.akiban.sql.StandardException;
import org.junit.Test;

public class RowCtorTest
{
    // no parexception means "pass"
    

    @Test
    public void regularCase() throws StandardException
    {
        System.out.println("regularCase");
        // smoke test
        // make sure it didn't break things that are working
        doTest("SELECT 3 IN (4,5,6)");
        
        
    }
    
    @Test
    public void columnTest() throws StandardException
    {
        System.out.println("columnTest");
        doTest("SELECT (2, 3, 4) IN ((5, 6, 7), (8, 9, 10))");
    }

    @Test
    public void mistmatchColumnTest() throws StandardException
    {
        System.out.println("mismatchColumn");
        // This should still pass
        // It's not the parser's job to check the number of columns
        // should be handle in InExpression
        //
        // Could add a field called 'depth' to RowConstructorNode
        // so some checking could be done here
        // (ie., the left list MUST be one level deeper than the right one)
        doTest("SELECT (2, 3, 4) IN (4, 5, 6)");
    }

    @Test
    public void nestedRows() throws StandardException
    {
        System.out.println("nested rows");
        doTest("SELECT ((2, 3), (4, 5)) in ((4, 5), (5, 7))");
    }

    @Test
    public void nonNestedRowsWithParens() throws StandardException
    {
        System.out.println("non nested rows");
        doTest("SELECT 1  in ((4, ((5))))");
    }

    static void doTest(String st) throws StandardException
    {
        SQLParser parser = new SQLParser();
        StatementNode node = parser.parseStatement(st);
        
        System.out.println("\n\n----------\n");
    }
}

