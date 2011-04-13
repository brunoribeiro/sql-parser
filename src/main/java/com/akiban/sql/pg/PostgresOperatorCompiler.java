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

package com.akiban.sql.pg;

import com.akiban.sql.parser.*;
import com.akiban.sql.compiler.*;

import com.akiban.sql.StandardException;
import com.akiban.sql.views.ViewDefinition;

import com.akiban.ais.model.AkibanInformationSchema;
import com.akiban.ais.model.Column;
import com.akiban.ais.model.Join;
import com.akiban.ais.model.UserTable;

import com.akiban.qp.expression.Compare;
import com.akiban.qp.expression.Comparison;
import com.akiban.qp.expression.Expression;
import com.akiban.qp.expression.Field;
import com.akiban.qp.expression.Literal;
import com.akiban.qp.persistitadapter.PersistitAdapter;
import com.akiban.qp.physicaloperator.Executable;
import com.akiban.qp.physicaloperator.Flatten_HKeyOrdered;
import com.akiban.qp.physicaloperator.GroupScan_Default;
import com.akiban.qp.physicaloperator.PhysicalOperator;
import com.akiban.qp.physicaloperator.Select_HKeyOrdered;
import com.akiban.qp.physicaloperator.StoreAdapter;
import com.akiban.qp.rowtype.RowType;
import com.akiban.qp.rowtype.Schema;
import com.akiban.qp.rowtype.UserTableRowType;

import com.akiban.server.service.ServiceManager;
import com.akiban.server.service.session.Session;
import com.akiban.server.store.PersistitStore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Compile SQL SELECT statements into operator trees if possible.
 */
public class PostgresOperatorCompiler implements PostgresStatementCompiler
{
  private static final Logger g_logger = LoggerFactory.getLogger(PostgresOperatorCompiler.class);

  private SQLParserContext m_parserContext;
  private NodeFactory m_nodeFactory;
  private AISBinder m_binder;
  private TypeComputer m_typeComputer;
  private BooleanNormalizer m_booleanNormalizer;
  private SubqueryFlattener m_subqueryFlattener;
  private Grouper m_grouper;
  private StoreAdapter m_adapter;

  public PostgresOperatorCompiler(SQLParser parser, 
                                  AkibanInformationSchema ais, String schema,
                                  Session session, ServiceManager serviceManager) {
    m_parserContext = parser;
    m_nodeFactory = m_parserContext.getNodeFactory();
    m_binder = new AISBinder(ais, schema);
    parser.setNodeFactory(new BindingNodeFactory(m_nodeFactory));
    m_typeComputer = new TypeComputer();
    m_booleanNormalizer = new BooleanNormalizer(parser);
    m_subqueryFlattener = new SubqueryFlattener(parser);
    m_grouper = new Grouper(parser);
    m_adapter = new PersistitAdapter(new Schema(ais),
                                     (PersistitStore)serviceManager.getStore(),
                                     session);
  }

  public void addView(ViewDefinition view) throws StandardException {
    m_binder.addView(view);
  }

  @Override
  public PostgresStatement compile(CursorNode cursor, int[] paramTypes)
      throws StandardException {
    // Get into bound & grouped form.
    m_binder.bind(cursor);
    cursor = (CursorNode)m_booleanNormalizer.normalize(cursor);
    m_typeComputer.compute(cursor);
    cursor = (CursorNode)m_subqueryFlattener.flatten(cursor);
    m_grouper.group(cursor);

    if (cursor.getOrderByList() != null)
      throw new StandardException("Unsupported ORDER BY");
    if (cursor.getOffsetClause() != null)
      throw new StandardException("Unsupported OFFSET");
    if (cursor.getFetchFirstClause() != null)
      throw new StandardException("Unsupported FETCH");
    if (cursor.getUpdateMode() == CursorNode.UpdateMode.UPDATE)
      throw new StandardException("Unsupported FOR UPDATE");

    SelectNode select = (SelectNode)cursor.getResultSetNode();
    if (select.getGroupByList() != null)
      throw new StandardException("Unsupported GROUP BY");
    if (select.isDistinct())
      throw new StandardException("Unsupported DISTINCT");
    if (select.hasWindows())
      throw new StandardException("Unsupported WINDOW");

    List<UserTable> tables = new ArrayList<UserTable>();
    GroupBinding group = null;
    for (FromTable fromTable : select.getFromList()) {
      if (!(fromTable instanceof FromBaseTable))
        throw new StandardException("Unsupported FROM non-table: " + fromTable);
      TableBinding tb = (TableBinding)fromTable.getUserData();
      if (tb == null) 
        throw new StandardException("Unsupported FROM table: " + fromTable);
      GroupBinding gb = tb.getGroupBinding();
      if (gb == null)
        throw new StandardException("Unsupported FROM non-group: " + fromTable);
      if (group == null)
        group = gb;
      else if (group != gb)
        throw new StandardException("Unsupported multiple groups");
      UserTable table = (UserTable)tb.getTable();
      tables.add(table);
    }
    Collections.sort(tables, new Comparator<UserTable>() {
                       public int compare(UserTable t1, UserTable t2) {
                         return t1.getDepth().compareTo(t2.getDepth());
                       }
                     });
    PhysicalOperator resultOperator = 
      new GroupScan_Default(m_adapter,
                            group.getGroup().getGroupTable());
    RowType resultRowType = null;
    Map<UserTable,Integer> fieldOffsets = new HashMap<UserTable,Integer>();
    UserTable prev = null;
    int nfields = 0;
    for (UserTable table : tables) {
      if (prev != null) {
        if (!isAncestorTable(prev, table))
          throw new StandardException("Unsupported branching group");
        // Join result so far to new child.
        Flatten_HKeyOrdered flatten = 
          new Flatten_HKeyOrdered(resultOperator,
                                  resultRowType,
                                  userTableRowType(table));
        resultOperator = flatten;
        resultRowType = flatten.rowType();
      }
      else {
        resultRowType = userTableRowType(table);
      }
      prev = table;
      fieldOffsets.put(table, nfields);
      nfields += table.getColumns().size();
    }

    ValueNode whereClause = select.getWhereClause();
    while (whereClause != null) {
      if (whereClause.isBooleanTrue()) break;
      if (!(whereClause instanceof AndNode))
        throw new StandardException("Unsupported complex WHERE");
      AndNode andNode = (AndNode)whereClause;
      whereClause = andNode.getRightOperand();
      ValueNode condition = andNode.getLeftOperand();
      if (m_grouper.getJoinConditions().contains(condition))
        continue;
      Comparison op;
      switch (condition.getNodeType()) {
      case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
        op = Comparison.EQ;
        break;
      case NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE:
        op = Comparison.GT;
        break;
      case NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE:
        op = Comparison.GE;
        break;
      case NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE:
        op = Comparison.LT;
        break;
      case NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE:
        op = Comparison.LE;
        break;
      default:
        throw new StandardException("Unsupported WHERE predicate");
      }
      BinaryOperatorNode binop = (BinaryOperatorNode)condition;
      Expression leftExpr = getExpression(binop.getLeftOperand(), fieldOffsets);
      Expression rightExpr = getExpression(binop.getRightOperand(), fieldOffsets);
      Compare predicate = new Compare(leftExpr, op, rightExpr);
      resultOperator = new Select_HKeyOrdered(resultOperator,
                                              resultRowType,
                                              predicate);
    }

    List<Column> resultColumns = new ArrayList<Column>();
    for (ResultColumn result : select.getResultColumns()) {
      if (!(result.getExpression() instanceof ColumnReference))
        throw new StandardException("Unsupported result column: " + result);
      ColumnReference cref = (ColumnReference)result.getExpression();
      ColumnBinding cb = (ColumnBinding)cref.getUserData();
      if (cb == null)
        throw new StandardException("Unsupported result column: " + result);
      Column column = cb.getColumn();
      if (column == null)
        throw new StandardException("Unsupported result column: " + result);
      resultColumns.add(column);
    }
    int ncols = resultColumns.size();
    int[] resultColumnOffsets = new int[ncols];
    for (int i = 0; i < ncols; i++) {
      Column column = resultColumns.get(i);
      UserTable table = column.getUserTable();
      resultColumnOffsets[i] = fieldOffsets.get(table) + column.getPosition();
    }

    g_logger.warn("Operator:\n{}", explainPlan(resultOperator));

    Executable executable = new Executable(m_adapter, resultOperator);
    return new PostgresOperatorStatement(executable, resultRowType, 
                                         resultColumns, resultColumnOffsets);
  }

  protected UserTableRowType userTableRowType(UserTable table) {
    return m_adapter.schema().userTableRowType(table);
  }

  protected Expression getExpression(ValueNode operand, 
                                     Map<UserTable,Integer> fieldOffsets)
      throws StandardException {
    if ((operand instanceof ColumnReference) &&
        (operand.getUserData() != null)) {
      Column column = ((ColumnBinding)operand.getUserData()).getColumn();
      if (column == null)
        throw new StandardException("Unsupported WHERE predicate on non-column");
      UserTable table = column.getUserTable();
      return new Field(fieldOffsets.get(table) + column.getPosition());
    }
    else if (operand instanceof ConstantNode) {
      Object value = ((ConstantNode)operand).getValue();
      if (value instanceof Integer)
        value = new Long(((Integer)value).intValue());
      return new Literal(value);
    }
    // TODO: Parameters: Literals but with later substitution somehow.
    else
      throw new StandardException("Unsupported WHERE predicate on non-constant");
  }

  /** Is t1 an ancestor of t2? */
  protected static boolean isAncestorTable(UserTable t1, UserTable t2) {
    while (true) {
      Join j = t2.getParentJoin();
      if (j == null)
        return false;
      UserTable parent = j.getParent();
      if (parent == null)
        return false;
      if (parent == t1)
        return true;
      t2 = parent;
    }
  }

  protected static String explainPlan(PhysicalOperator operator) {
    StringBuilder sb = new StringBuilder();
    explainPlan(operator, sb, 0);
    return sb.toString();
  }

  protected static void explainPlan(PhysicalOperator operator, 
                                    StringBuilder into, int depth) {
    for (int i = 0; i < depth; i++)
      into.append("  ");
    if (operator instanceof Flatten_HKeyOrdered) {
      Flatten_HKeyOrdered flatten = (Flatten_HKeyOrdered)operator;
      into.append("Flatten_HKeyOrdered(");
      into.append(flatten.parentType);
      into.append(",");
      into.append(flatten.childType);
      into.append(")\n");
      explainPlan(flatten.inputOperator, into, depth+1);
    }
    else if (operator instanceof Select_HKeyOrdered) {
      Select_HKeyOrdered select = (Select_HKeyOrdered)operator;
      into.append("Select_HKeyOrdered(");
      into.append(select.predicateRowType);
      into.append(",");
      explainExpression(select.predicate, into);
      into.append(")\n");
      explainPlan(select.inputOperator, into, depth+1);
    }
    else if (operator instanceof GroupScan_Default) {
      GroupScan_Default group = (GroupScan_Default)operator;
      into.append("GroupScan_Default(");
      into.append(group.groupTable);
      into.append(")\n");
    }
    else {
      into.append(operator);
      into.append(")\n");
    }
  }

  protected static void explainExpression(Expression expression,
                                          StringBuilder into) {
    if (expression instanceof Field) {
      Field field = (Field)expression;
      into.append("Field(");
      into.append(field.position);
      into.append(")");
    }
    else if (expression instanceof Literal) {
      Literal literal = (Literal)expression;
      into.append("Literal(");
      into.append(literal.value);
      into.append(")");
    }
    else if (expression instanceof Compare) {
      Compare compare = (Compare)expression;
      into.append("Compare(");
      explainExpression(compare.left, into);
      into.append(" ");
      into.append(compare.comparison);
      into.append(" ");
      explainExpression(compare.right, into);
      into.append(")");
    }
    else
      into.append(expression);
  }

}
