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

package com.akiban.sql.unparser;

import com.akiban.sql.TestBase;

import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class ParseMultipleTest extends TestBase implements TestBase.GenerateAndCheckResult
{
    public static final File RESOURCE_DIR = 
        new File(NodeToStringTest.RESOURCE_DIR, "multiple");

    protected SQLParser parser;
    protected NodeToString unparser;

    @Before
    public void before() throws Exception {
        parser = new SQLParser();
        unparser = new NodeToString();
    }

    @Parameters
    public static Collection<Object[]> statements() throws Exception {
        return sqlAndExpected(RESOURCE_DIR);
    }

    public ParseMultipleTest(String caseName, String sql, 
                             String expected, String error) {
        super(caseName, sql, expected, error);
    }

    @Test
    public void testParseMultiple() throws Exception {
        generateAndCheckResult();
    }

    @Override
    public String generateResult() throws Exception {
        List<StatementNode> stmts = parser.parseStatements(sql);
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < stmts.size(); i++) {
            if (i > 0) str.append("\n");
            str.append("[" + i + "]: ");
            str.append(unparser.toString(stmts.get(i)));
            str.append(";");
        }
        return str.toString();
    }

    @Override
    public void checkResult(String result) {
        assertEquals(caseName, expected, result);
    }

}
