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

package com.akiban.sql.unparser;

import com.akiban.sql.TestBase;

import com.akiban.sql.StandardException;
import com.akiban.sql.compiler.BoundNodeToString;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.SQLParser;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import java.io.File;

public class NodeToStringTest extends TestBase
{
  public static final File RESOURCE_DIR = 
    new File("src/test/resources/"
             + NodeToStringTest.class.getPackage().getName().replace('.', '/'));

  protected SQLParser parser;
  protected BoundNodeToString unparser;

  @Before
  public void before() throws Exception {
    parser = new SQLParser();
    unparser = new BoundNodeToString();
  }

  @Test
  public void testUnparser() throws Exception {
    unparser.setUseBindings(false);

    int npass = 0, nfail = 0;
    File[] sqlFiles = listSQLFiles(RESOURCE_DIR);
    for (File sqlFile : sqlFiles) {
      String sql_in = fileContents(sqlFile).trim();
      StatementNode stmt = parser.parseStatement(sql_in);
      String sql_out = unparser.toString(stmt);
      String expected = fileContents(expectedFile(sqlFile));
      if (sql_out.equals(expected.trim()))
        npass++;
      else {
        System.out.println("Mismatch for " + sqlFile);
        System.out.println(sql_in);
        System.out.println(expected);
        System.out.println(sql_out);
        nfail++;
      }
    }

    if (nfail > 0)
      fail(nfail + " parses did not match.");
  }
}
