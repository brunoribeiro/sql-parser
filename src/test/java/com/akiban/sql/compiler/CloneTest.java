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

package com.akiban.sql.compiler;

import com.akiban.sql.parser.StatementNode;

import org.junit.Test;
import static junit.framework.Assert.*;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CloneTest extends ASTTransformTestBase
{
  public static final File RESOURCE_DIR = 
    new File(ASTTransformTestBase.RESOURCE_DIR, "clone");

  @Parameters
  public static Collection<Object[]> statements() throws Exception {
    return sqlAndExpected(RESOURCE_DIR);
  }

  public CloneTest(String sql, String expected) {
    super(sql, expected);
  }

  @Test
  public void testClone() throws Exception {
    StatementNode stmt = parser.parseStatement(sql);
    stmt = (StatementNode)parser.getNodeFactory().copyNode(stmt, parser);
    assertEquals(expected, unparser.toString(stmt));
  }

}
