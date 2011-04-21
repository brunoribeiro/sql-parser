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

import com.akiban.sql.CompareWithoutHashes;
import com.akiban.sql.TestBase;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.compiler.AISBinder;
import com.akiban.sql.compiler.BindingNodeFactory;
import com.akiban.sql.compiler.BooleanNormalizer;
import com.akiban.sql.compiler.BoundNodeToString;
import com.akiban.sql.compiler.Grouper;
import com.akiban.sql.compiler.SubqueryFlattener;
import com.akiban.sql.compiler.TypeComputer;
import com.akiban.sql.views.ViewDefinition;

import com.akiban.ais.ddl.SchemaDef;
import com.akiban.ais.ddl.SchemaDefToAis;
import com.akiban.ais.model.AkibanInformationSchema;

import org.junit.Before;
import org.junit.Test;
import static junit.framework.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

public class ASTTransformTest extends TestBase
{
  public static final File RESOURCE_DIR = 
    new File("src/test/resources/"
             + ASTTransformTest.class.getPackage().getName().replace('.', '/'));

  protected SQLParser parser;
  protected BoundNodeToString unparser;
  protected AISBinder binder;
  protected TypeComputer typeComputer;
  protected BooleanNormalizer booleanNormalizer;
  protected SubqueryFlattener subqueryFlattener;
  protected Grouper grouper;

  @Before
  public void before() throws Exception {
    parser = new SQLParser();
    parser.setNodeFactory(new BindingNodeFactory(parser.getNodeFactory()));
    unparser = new BoundNodeToString();
    typeComputer = new TypeComputer();
    booleanNormalizer = new BooleanNormalizer(parser);
    subqueryFlattener = new SubqueryFlattener(parser);
    grouper = new Grouper(parser);
  }

  interface Transformer {
    StatementNode transform(StatementNode stmt) throws StandardException;
  }

  protected void loadSchema(File schema) throws Exception {
    String sql = fileContents(schema);
    SchemaDef schemaDef = SchemaDef.parseSchema("use user; " + sql);
    SchemaDefToAis toAis = new SchemaDefToAis(schemaDef, false);
    AkibanInformationSchema ais = toAis.getAis();
    binder = new AISBinder(ais, "user");
  }

  protected void loadView(File view) throws Exception {
    String sql = fileContents(view);
    binder.addView(new ViewDefinition(sql, parser));
  }

  protected String getTree(StatementNode stmt) throws IOException {
    StringWriter str = new StringWriter();
    stmt.treePrint(str);
    return str.toString().trim();
  }

  protected void testFiles(File dir, Transformer xform) 
      throws Exception {
    testFiles(dir, xform, false, null);
  }

  protected void testFiles(File dir, Transformer xform, 
                           boolean expectTree, String compareRegex)
      throws Exception {
    if (expectTree && (compareRegex == null))
      compareRegex = CompareWithoutHashes.HASH_REGEX;
    int npass = 0, nfail = 0;
    File[] sqlFiles = listSQLFiles(dir);
    for (File sqlFile : sqlFiles) {
      String sql_in = fileContents(sqlFile).trim();
      String sql_out;
      try {
        StatementNode stmt = parser.parseStatement(sql_in);
        stmt = xform.transform(stmt);
        if (expectTree)
          sql_out = getTree(stmt);
        else
          sql_out = unparser.toString(stmt);
      }
      catch (Exception ex) {
        System.out.println("Error for " + sqlFile);
        ex.printStackTrace(System.out);
        nfail++;
        continue;
      }
      String expected = fileContents(expectedFile(sqlFile)).trim();
      if ((compareRegex == null) ?
          sql_out.equals(expected) :
          new CompareWithoutHashes(compareRegex).match(new StringReader(sql_out), 
                                                       new StringReader(expected)))
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
      fail(nfail + " transforms did not match.");
  }
  
  @Test
  public void testClone() throws Exception {
    File dir = new File(RESOURCE_DIR, "clone");
    unparser.setUseBindings(false);
    testFiles(dir,
              new Transformer() {
                public StatementNode transform(StatementNode stmt) 
                    throws StandardException {
                  return (StatementNode)parser.getNodeFactory().copyNode(stmt, parser);
                }
              });
  }

  @Test
  public void testBinding() throws Exception {
    File dir = new File(RESOURCE_DIR, "binding");
    loadSchema(new File(dir, "schema.ddl"));
    testFiles(dir,
              new Transformer() {
                public StatementNode transform(StatementNode stmt) 
                    throws StandardException {
                  binder.bind(stmt);
                  return stmt;
                }
              },
              true, null);
  }

  @Test
  public void testView() throws Exception {
    File dir = new File(RESOURCE_DIR, "view");
    loadSchema(new File(dir, "schema.ddl"));
    loadView(new File(dir, "view-1.ddl"));
    testFiles(dir,
              new Transformer() {
                public StatementNode transform(StatementNode stmt) 
                    throws StandardException {
                  binder.bind(stmt);
                  return stmt;
                }
              },
              true, null);
  }

  @Test
  public void testNormalizer() throws Exception {
    File dir = new File(RESOURCE_DIR, "normalize");
    unparser.setUseBindings(false);
    testFiles(dir,
              new Transformer() {
                public StatementNode transform(StatementNode stmt) 
                    throws StandardException {
                  return booleanNormalizer.normalize(stmt);
                }
              });
  }

  @Test
  public void testFlattener() throws Exception {
    File dir = new File(RESOURCE_DIR, "flatten");
    loadSchema(new File(dir, "schema.ddl"));
    loadView(new File(dir, "view-1.ddl"));
    unparser.setUseBindings(true);
    testFiles(dir,
              new Transformer() {
                public StatementNode transform(StatementNode stmt) 
                    throws StandardException {
                  binder.bind(stmt);
                  stmt = booleanNormalizer.normalize(stmt);
                  typeComputer.compute(stmt);
                  return subqueryFlattener.flatten(stmt);
                }
              });
  }

  @Test
  public void testGrouper() throws Exception {
    File dir = new File(RESOURCE_DIR, "group");
    loadSchema(new File(dir, "schema.ddl"));
    loadView(new File(dir, "view-1.ddl"));
    unparser.setUseBindings(false);
    testFiles(dir,
              new Transformer() {
                public StatementNode transform(StatementNode stmt) 
                    throws StandardException {
                  binder.bind(stmt);
                  stmt = booleanNormalizer.normalize(stmt);
                  typeComputer.compute(stmt);
                  stmt = subqueryFlattener.flatten(stmt);
                  grouper.group(stmt);
                  grouper.rewrite(stmt);
                  return stmt;
                }
              },
              false, "_G_\\d+");
  }

}
