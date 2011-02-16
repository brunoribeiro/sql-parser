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

import com.akiban.sql.parser.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.types.DataTypeDescriptor;
import com.akiban.sql.types.TypeId;

import com.akiban.ais.model.AkibanInformationSchema;
import com.akiban.ais.model.Column;
import com.akiban.ais.model.Table;
import com.akiban.ais.model.Type;

import java.util.*;

/** Bind objects to Akiban schema. */
public class AISBinder implements Visitor
{
  private AkibanInformationSchema ais;
  private String defaultSchemaName;
  private Stack<BindingContext> bindingContexts;

  public AISBinder(AkibanInformationSchema ais, String defaultSchemaName) {
    this.ais = ais;
    this.defaultSchemaName = defaultSchemaName;
    this.bindingContexts = new Stack<BindingContext>();
  }

  public void bind(StatementNode stmt) throws StandardException {
    stmt.accept(this);
  }
  
  /* Hierarchical Visitor */

  public boolean visitBefore(QueryTreeNode node) throws StandardException {
    switch (node.getNodeType()) {
    case NodeTypes.CURSOR_NODE:
    case NodeTypes.FROM_SUBQUERY:
      pushBindingContext();
      break;
    case NodeTypes.SUBQUERY_NODE:
      {
        SubqueryNode subqueryNode = (SubqueryNode)node;
        // The LHS of a subquery operator is bound in the outer context.
        if (subqueryNode.getLeftOperand() != null)
          subqueryNode.getLeftOperand().accept(this);
        pushBindingContext();
      }
      break;
    case NodeTypes.SELECT_NODE:
      selectNode((SelectNode)node);
      break;
    case NodeTypes.COLUMN_REFERENCE:
      columnReference((ColumnReference)node);
      break;
    }
    return true;
  }

  public void visitAfter(QueryTreeNode node) throws StandardException {
    switch (node.getNodeType()) {
    case NodeTypes.CURSOR_NODE:
    case NodeTypes.FROM_SUBQUERY:
    case NodeTypes.SUBQUERY_NODE:
      popBindingContext();
      break;
    }
  }

  /* Specific node types */

  protected void selectNode(SelectNode selectNode) throws StandardException {
    BindingContext bindingContext = getBindingContext();
    for (FromTable fromTable : selectNode.getFromList()) {
      switch (fromTable.getNodeType()) {
      case NodeTypes.FROM_BASE_TABLE:
        fromBaseTable((FromBaseTable)fromTable);
        break;
      }
    }
    for (FromTable fromTable : selectNode.getFromList()) {
      bindingContext.tables.add(fromTable);
      if (fromTable.getCorrelationName() != null) {
        if (bindingContext.correlationNames.put(fromTable.getCorrelationName(), 
                                                fromTable) != null) {
          throw new StandardException("More than one use of " + 
                                      fromTable.getCorrelationName() +
                                      " as correlation name");
        }
      }
    }    
  }

  protected void fromBaseTable(FromBaseTable fromBaseTable) throws StandardException {
    TableName tableName = fromBaseTable.getTableName();
    Table table = lookupTableName(tableName);
    tableName.setUserData(table);
    // TODO: Something higher level on the fromBaseTable.
  }
  
  protected void columnReference(ColumnReference columnReference) 
      throws StandardException {
    ColumnBinding columnBinding = null;
    String columnName = columnReference.getColumnName();
    if (columnReference.getTableNameNode() != null) {
      FromTable fromTable = findFromTable(columnReference.getTableNameNode());
      columnBinding = getColumnBinding(fromTable, columnName);
      if (columnBinding == null)
        throw new StandardException("Column " + columnName +
                                    " not found in " + fromTable.getExposedName());
    }
    else {
      for (BindingContext bindingContext : bindingContexts) {
        for (FromTable fromTable : bindingContext.tables) {
          ColumnBinding aColumnBinding = getColumnBinding(fromTable, columnName);
          if (aColumnBinding != null) {
            if (columnBinding != null)
              throw new StandardException("Column " + columnName + " is ambiguous");
            else
              columnBinding = aColumnBinding;
          }
        }
      }
      if (columnBinding == null)
        throw new StandardException("Column " + columnName + " not found");
    }
    columnReference.setUserData(columnBinding);
  }

  protected Table lookupTableName(TableName tableName)
      throws StandardException {
    String schemaName = tableName.getSchemaName();
    if (schemaName == null)
      schemaName = defaultSchemaName;
    Table result = ais.getUserTable(schemaName, 
                                    // TODO: Akiban DB thinks it's case sensitive.
                                    tableName.getTableName().toLowerCase());
    if (result == null)
      throw new StandardException("Table " + tableName.getFullTableName() +
                                  " not found");
    return result;
  }

  protected FromTable findFromTable(TableName tableNameNode) throws StandardException {
    String schemaName = tableNameNode.getSchemaName();
    String tableName = tableNameNode.getTableName();
    if (schemaName == null) {
      FromTable fromTable = getBindingContext().correlationNames.get(tableName);
      if (fromTable != null)
        return fromTable;

      schemaName = defaultSchemaName;
    }
    FromTable result = null;
    for (BindingContext bindingContext : bindingContexts) {
      for (FromTable fromTable : bindingContext.tables) {
        if ((fromTable instanceof FromBaseTable) &&
            // Not allowed to reference correlated by underlying name.
            (fromTable.getCorrelationName() == null)) {
          FromBaseTable fromBaseTable = (FromBaseTable)fromTable;
          Table table = (Table)fromBaseTable.getTableName().getUserData();
          assert (table != null) : "table not bound yet";
          if (table.getName().getSchemaName().equals(schemaName) &&
              table.getName().getTableName().equals(tableName)) {
            if (result != null)
              throw new StandardException("Ambiguous table " + tableName);
            else
              result = fromBaseTable;
          }
        }
      }
    }
    if (result == null)
      throw new StandardException("Table " + tableNameNode + " not found");
    return result;
  }

  // TODO: Move to another file.
  static class ColumnBinding {
    private FromTable fromTable;
    private Column column;
    private ResultColumn resultColumn;
    
    public ColumnBinding(FromTable fromTable, Column column) {
      this.fromTable = fromTable;
      this.column = column;
    }
    public ColumnBinding(FromTable fromTable, ResultColumn resultColumn) {
      this.fromTable = fromTable;
      this.resultColumn = resultColumn;
    }

    public String toString() {
      StringBuffer result = new StringBuffer();
      if (resultColumn != null) {
        result.append(resultColumn.getClass().getName());
        result.append('@');
        result.append(Integer.toHexString(resultColumn.hashCode()));
      }
      else
        result.append(column);
      result.append(" from ");
      result.append(fromTable.getClass().getName());
      result.append('@');
      result.append(Integer.toHexString(fromTable.hashCode()));
      return result.toString();
    }
  }

  protected ColumnBinding getColumnBinding(FromTable fromTable, String columnName)
      throws StandardException {
    if (fromTable instanceof FromBaseTable) {
      FromBaseTable fromBaseTable = (FromBaseTable)fromTable;
      Table table = (Table)fromBaseTable.getTableName().getUserData();
      assert (table != null) : "table not bound yet";
      Column column = table.getColumn(columnName);
      if (column == null)
        return null;
      return new ColumnBinding(fromTable, column);
    }
    else if (fromTable instanceof FromSubquery) {
      FromSubquery fromSubquery = (FromSubquery)fromTable;
      ResultColumn resultColumn = fromSubquery.getSubquery().getResultColumns()
        .getResultColumn(columnName);
      if (resultColumn == null)
        return null;
      return new ColumnBinding(fromTable, resultColumn);
    }
    else {
      assert false;
      return null;
    }
  }

  protected static class BindingContext {
    Collection<FromTable> tables = new ArrayList<FromTable>();
    Map<String,FromTable> correlationNames = new HashMap<String,FromTable>();
  }

  protected BindingContext getBindingContext() {
    return bindingContexts.peek();
  }
  protected void pushBindingContext() {
    BindingContext next = new BindingContext();
    if (!bindingContexts.empty()) {
      next.correlationNames.putAll(bindingContexts.peek().correlationNames);
    }
    bindingContexts.push(next);
  }
  protected void popBindingContext() {
    bindingContexts.pop();
  }

  /* Visitor interface.
     This is messy. Perhaps there should be an abstract class which makes the common
     Visitor interface into a Hierarchical Vistor pattern. 
  */

  // To understand why this works, see QueryTreeNode.accept().
  public Visitable visit(Visitable node) throws StandardException {
    visitAfter((QueryTreeNode)node);
    return node;
  }

  public boolean skipChildren(Visitable node) throws StandardException {
    return ! visitBefore((QueryTreeNode)node);
  }

  public boolean visitChildrenFirst(Visitable node) {
    return true;
  }
  public boolean stopTraversal() {
    return false;
  }

  // TODO: Temporary low-budget testing.
  public static void main(String[] args) throws Exception {
    AkibanInformationSchema ais = new com.akiban.ais.ddl.DDLSource()
      .buildAISFromString(args[0]);
    AISBinder binder = new AISBinder(ais, args[1]);
    SQLParser p = new SQLParser();
    for (int i = 2; i < args.length; i++) {
      String arg = args[i];
      System.out.println("=====");
      System.out.println(arg);
      try {
        StatementNode stmt = p.parseStatement(arg);
        binder.bind(stmt);
        stmt.treePrint();
      }
      catch (StandardException ex) {
        ex.printStackTrace();
      }
    }
  }
}
