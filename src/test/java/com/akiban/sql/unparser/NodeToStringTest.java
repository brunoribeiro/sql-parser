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

@RunWith(Parameterized.class)
public class NodeToStringTest extends TestBase
{
    public static final File RESOURCE_DIR = 
        new File("src/test/resources/"
                 + NodeToStringTest.class.getPackage().getName().replace('.', '/'));

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

    public NodeToStringTest(String caseName, String sql, 
                            String expected, String error) {
        super(caseName, sql, expected, error);
    }

    @Test
    public void testUnparser() throws Exception {
        String result = null;
        Exception errorResult = null;
        try {
            StatementNode stmt = parser.parseStatement(sql);
            result = unparser.toString(stmt);
        }
        catch (Exception ex) {
            errorResult = ex;
        }
        if (error != null) {
            if (errorResult == null)
                fail(caseName + ": error expected but none thrown");
            else
                assertEquals(caseName, error, errorResult.toString());
        }
        else if (errorResult != null) {
            throw errorResult;
        }
        else {
            assertEquals(caseName, expected, result);
        }
    }

}
