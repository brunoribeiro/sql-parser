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

package com.akiban.sql;

import org.junit.Ignore;
import static junit.framework.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

@Ignore
public class TestBase
{
  protected TestBase() {
  }

  protected String sql, expected;

  protected TestBase(String sql, String expected) {
    this.sql = sql;
    this.expected = expected;
  }

  public static File[] listSQLFiles(File dir) {
    File[] result = dir.listFiles(new RegexFilenameFilter(".*\\.sql"));
    Arrays.sort(result, new Comparator<File>() {
                  public int compare(File f1, File f2) {
                    return f1.getName().compareTo(f2.getName());
                  }
                });
    return result;
  }

  public static File expectedFile(File sqlFile) {
    return new File(sqlFile.getParentFile(),
                    sqlFile.getName().replace(".sql", ".expected"));
  }

  public static String fileContents(File file) throws IOException {
    FileReader reader = null;
    try {
      reader = new FileReader(file);
      StringBuilder str = new StringBuilder();
      char[] buf = new char[128];
      while (true) {
        int nc = reader.read(buf);
        if (nc < 0) break;
        str.append(buf, 0, nc);
      }
      return str.toString();
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        }
        catch (IOException ex) {
        }
      }
    }
  }

  public static Collection<Object[]> sqlAndExpected(File dir) throws IOException {
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File sqlFile : listSQLFiles(dir)) {
      result.add(new Object[] {
                   fileContents(sqlFile),
                   fileContents(expectedFile(sqlFile))
                 });
    }
    return result;
  }

  protected static void assertEqualsWithoutHashes(String expected, String actual) 
      throws IOException {
    assertEqualsWithoutPattern(expected, actual, CompareWithoutHashes.HASH_REGEX);
  }

  protected static void assertEqualsWithoutPattern(String expected, String actual, 
                                                   String regex) 
      throws IOException {
    if (!new CompareWithoutHashes(regex).match(new StringReader(expected), 
                                               new StringReader(actual)))
      fail("expected='" + expected + "' actual='" + actual + "'");
  }

}