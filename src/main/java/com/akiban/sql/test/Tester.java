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

package com.akiban.sql.test;

import com.akiban.sql.StandardException;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.unparser.NodeToString;
import com.akiban.sql.compiler.AISBinder;
import com.akiban.sql.compiler.BooleanNormalizer;
import com.akiban.sql.compiler.TypeComputer;

import com.akiban.ais.model.AkibanInformationSchema;

import java.util.*;

/** Standalone testing. */
public class Tester
{
  enum Action { 
    ECHO, PARSE, 
    PRINT_SQL, PRINT_TREE,
    BIND, COMPUTE_TYPES,
    BOOLEAN_NORMALIZE
  }

  List<Action> actions;
  SQLParser parser;
  NodeToString unparser;
  AISBinder binder;
  TypeComputer typeComputer;
  BooleanNormalizer booleanNormalizer;

  public Tester() {
    actions = new ArrayList<Action>();
    parser = new SQLParser();
    unparser = new NodeToString();
    typeComputer = new TypeComputer();
    booleanNormalizer = new BooleanNormalizer(parser);
  }

  public void addAction(Action action) {
    actions.add(action);
  }

  public void process(String sql) throws Exception {
    StatementNode stmt = null;
    for (Action action : actions) {
      switch (action) {
      case ECHO:
        System.out.println("=====");
        System.out.println(sql);
        break;
      case PARSE:
        stmt = parser.parseStatement(sql);
        break;
      case PRINT_SQL:
        System.out.println(unparser.toString(stmt));
        break;
      case PRINT_TREE:
        stmt.treePrint();
        break;
      case BIND:
        binder.bind(stmt);
        break;
      case COMPUTE_TYPES:
        typeComputer.compute(stmt);
        break;
      case BOOLEAN_NORMALIZE:
        stmt = (StatementNode)stmt.accept(booleanNormalizer);
        break;
      }
    }
  }

  public void setSchema(String sql) throws Exception {
    AkibanInformationSchema ais = new com.akiban.ais.ddl.DDLSource()
      .buildAISFromString("use user; " + sql);
    binder = new AISBinder(ais, "user");
  }

  public static void main(String[] args) throws Exception {
    Tester tester = new Tester();
    tester.addAction(Action.ECHO);
    tester.addAction(Action.PARSE);
    int i = 0;
    while (i < args.length) {
      String arg = args[i++];
      if (arg.startsWith("-")) {
        if ("-print".equals(arg))
          tester.addAction(Action.PRINT_SQL);
        else if ("-tree".equals(arg))
          tester.addAction(Action.PRINT_TREE);
        else if ("-bind".equals(arg)) {
          tester.setSchema(args[i++]);
          tester.addAction(Action.BIND);
        }
        else if ("-types".equals(arg))
          tester.addAction(Action.COMPUTE_TYPES);
        else if ("-boolean".equals(arg))
          tester.addAction(Action.BOOLEAN_NORMALIZE);
        else
          throw new Exception("Unknown switch: " + arg);
      }
      else {
        try {
          tester.process(arg);
        }
        catch (StandardException ex) {
          System.out.flush();
          ex.printStackTrace();
        }
      }
    }
  }
}
