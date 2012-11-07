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
import org.junit.Test;

public class RowCtorTest
{
    // no parexception means "pass"
    

    @Test
    public void regularCase() throws StandardException
    {
        // smoke test
        // make sure it didn't break things that are working
        doTest("SELECT 3 IN (4,5,6)");
    }
 
    @Test
    public void testWithBoolOp1() throws StandardException
    {
        doTest("select ((1 and 2) and 3) IN (2, 4, 5)");
    }
    
    // TODO: not passing because of the [5 or 6]
    // why?
//    @Test
//    public void testWithBoolOp2() throws StandardException
//    {
//        doTest("select ((1 or 2) and 3) IN (2, 4, 5 or 6)");
//    }
        
    @Test
    public void testWithTables() throws StandardException
    {
        doTest(" select (c1, c2) in ((1, 3), (c3, c4)) from t");
    }

    @Test
    public void matchingColumn() throws StandardException
    {
        doTest("SELECT (2, 3, 4) IN ((5, 6, 7), (8, (9, 10, 11)))");
    }

    @Test
    public void mistmatchColumnTest() throws StandardException
    {
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
        doTest("SELECT ((2, 3), (4, 5)) in ((4, 5), (5, 7))");
    }

    @Test
    public void nonNestedRowsWithParens() throws StandardException
    {
        doTest("SELECT 1  in ((4, ((5))))");
    }

    static void doTest(String st) throws StandardException
    {
        SQLParser parser = new SQLParser();
        StatementNode node = parser.parseStatement(st);
    }
}

