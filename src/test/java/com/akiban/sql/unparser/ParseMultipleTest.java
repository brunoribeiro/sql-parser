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
