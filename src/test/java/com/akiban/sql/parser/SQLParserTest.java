/* Copyright (C) 2011 Akiban Technologies Inc.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.akiban.sql.parser;

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
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;

@RunWith(Parameterized.class)
public class SQLParserTest extends TestBase
{
  public static final File RESOURCE_DIR = 
    new File("src/test/resources/"
             + SQLParserTest.class.getPackage().getName().replace('.', '/'));

  protected SQLParser parser;

  @Before
  public void before() throws Exception {
    parser = new SQLParser();
  }

  protected String getTree(StatementNode stmt) throws IOException {
    StringWriter str = new StringWriter();
    stmt.treePrint(str);
    return str.toString().trim();
  }

  @Parameters
  public static Collection<Object[]> queries() throws Exception {
    return sqlAndExpected(RESOURCE_DIR);
  }

  public SQLParserTest(String sql, String expected) {
    super(sql, expected);
  }

  @Test
  public void testParser() throws Exception {
    StatementNode stmt = parser.parseStatement(sql);
    assertEqualsWithoutHashes(expected, getTree(stmt));
  }
}
