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

import com.akiban.sql.CompareWithoutHashes;
import com.akiban.sql.TestBase;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.StatementNode;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

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

  protected boolean compare(String tree, String expected) throws IOException {
    return new CompareWithoutHashes().match(new StringReader(tree), 
                                            new StringReader(expected));
  }

  protected void testFiles(File dir) throws Exception {
    int npass = 0, nfail = 0;
    File[] sqlFiles = listSQLFiles(dir);
    for (File sqlFile : sqlFiles) {
      String sql = fileContents(sqlFile).trim();
      String tree;
      try {
        StatementNode stmt = parser.parseStatement(sql);
        tree = getTree(stmt);
      }
      catch (Exception ex) {
        System.out.println("Error for " + sqlFile);
        ex.printStackTrace(System.out);
        nfail++;
        continue;
      }
      String expected = fileContents(expectedFile(sqlFile)).trim();
      if (compare(tree, expected))
        npass++;
      else {
        System.out.println("Mismatch for " + sqlFile);
        System.out.println(sql);
        System.out.println(expected);
        System.out.println(tree);
        nfail++;
      }
    }

    if (nfail > 0)
      fail(nfail + " parses did not match.");
  }

  @Test
  public void testParser() throws Exception {
    testFiles(RESOURCE_DIR);
  }
}
