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
import com.akiban.ais.model.GroupTable;
import com.akiban.ais.model.Index;
import com.akiban.ais.model.IndexColumn;
import com.akiban.ais.model.Join;
import com.akiban.ais.model.UserTable;

import com.akiban.qp.expression.Comparison;
import com.akiban.qp.expression.Expression;
import com.akiban.qp.expression.IndexBound;
import com.akiban.qp.expression.IndexKeyRange;
import static com.akiban.qp.expression.API.*;

import com.akiban.qp.persistitadapter.PersistitAdapter;
import com.akiban.qp.physicaloperator.PhysicalOperator;
import com.akiban.qp.physicaloperator.StoreAdapter;
import static com.akiban.qp.physicaloperator.API.*;

import com.akiban.qp.row.HKey;
import com.akiban.qp.row.RowBase;
import com.akiban.qp.rowtype.IndexKeyType;
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
    GroupTable groupTable = group.getGroup().getGroupTable();
    Collections.sort(tables, new Comparator<UserTable>() {
                       public int compare(UserTable t1, UserTable t2) {
                         return t1.getDepth().compareTo(t2.getDepth());
                       }
                     });
    Set<BinaryOperatorNode> indexConditions = new HashSet<BinaryOperatorNode>();
    Index index = null;
    if (select.getWhereClause() != null) {
      // TODO: Put ColumnReferences on the left of any condition with constant in WHERE,
      // changing operand as necessary.
      index = pickBestIndex(tables, select.getWhereClause(), indexConditions);
    }
    PhysicalOperator resultOperator, boundOperator;
    Object resultBinding;
    if (index == null) {
      resultOperator = groupScan_Default(m_adapter, groupTable);
      resultBinding = null;
      boundOperator = null;
    }
    else {
      // All selected rows above this need to be output by hkey left
      // segment random access.
      List<RowType> addAncestors = new ArrayList<RowType>();
      for (UserTable table : tables) {
        if (table == index.getTable())
          break;
        addAncestors.add(userTableRowType(table));
      }
      boundOperator = indexScan_Default(index);
      resultOperator = indexLookup_Default(boundOperator, groupTable, addAncestors);
      resultBinding = getIndexKeyRange(index, indexConditions);
    }
    RowType resultRowType = null;
    Map<UserTable,Integer> fieldOffsets = new HashMap<UserTable,Integer>();
    UserTable prev = null;
    int nfields = 0;
    // TODO: Tables that are only used for join conditions (no
    // predicates or result columns) can be skipped in flatten (and in
    // index ancestors above).
    for (UserTable table : tables) {
      if (prev != null) {
        if (!isAncestorTable(prev, table))
          throw new StandardException("Unsupported branching group");
        // Join result so far to new child.
        resultOperator = flatten_HKeyOrdered(resultOperator,
                                             resultRowType,
                                             userTableRowType(table));
        resultRowType = resultOperator.rowType();
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
      if (indexConditions.contains(binop))
        continue;
      Expression leftExpr = getExpression(binop.getLeftOperand(), fieldOffsets);
      Expression rightExpr = getExpression(binop.getRightOperand(), fieldOffsets);
      Expression predicate = compare(leftExpr, op, rightExpr);
      resultOperator = select_HKeyOrdered(resultOperator,
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

    g_logger.warn("Operator:\n{} {}", resultOperator, resultBinding);

    return new PostgresOperatorStatement(m_adapter, resultOperator, 
                                         resultBinding, boundOperator, resultRowType, 
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
      return field(fieldOffsets.get(table) + column.getPosition());
    }
    else if (operand instanceof ConstantNode) {
      Object value = ((ConstantNode)operand).getValue();
      if (value instanceof Integer)
        value = new Long(((Integer)value).intValue());
      return literal(value);
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

  protected Index pickBestIndex(List<UserTable> tables, 
                                ValueNode whereClause,
                                Set<BinaryOperatorNode> indexConditions) {
    if (whereClause == null) 
      return null;
    
    Index bestIndex = null;
    Set<BinaryOperatorNode> bestIndexConditions = null;
    for (UserTable table : tables) {
      for (Index index : table.getIndexes()) { // TODO: getIndexesIncludingInternal()
        Set<BinaryOperatorNode> matchingConditions = matchIndexConditions(index, 
                                                                          whereClause);
        if (matchingConditions.size() > ((bestIndex == null) ? 0 : 
                                         bestIndexConditions.size())) {
          bestIndex = index;
          bestIndexConditions = matchingConditions;
        }
      }
    }
    if (bestIndex != null)
      indexConditions.addAll(bestIndexConditions);
    return bestIndex;
  }

  // Return where conditions matching a left subset of index columns of given index.
  protected Set<BinaryOperatorNode> matchIndexConditions(Index index,
                                                         ValueNode whereClause) {
    Set<BinaryOperatorNode> result = null;
    boolean alleq = true;
    for (IndexColumn indexColumn : index.getColumns()) {
      Column column = indexColumn.getColumn();
      Set<BinaryOperatorNode> match = matchColumnConditions(column, whereClause);
      if (match == null)
        break;
      else if (result == null)
        result = match;
      else
        result.addAll(match);
      if (alleq) {
        for (ValueNode condition : match) {
          switch (condition.getNodeType()) {
          case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
            break;
          default:
            alleq = false;
            break;
          }
          if (!alleq) break;
        }
      }
      if (!alleq) break;
    }
    if (result == null)
      result = Collections.emptySet();
    return result;
  }

  // Return where conditions matching given column in supported comparison.
  protected Set<BinaryOperatorNode> matchColumnConditions(Column column,
                                                          ValueNode whereClause) {
    Set<BinaryOperatorNode> result = null;
    while (whereClause != null) {
      if (whereClause.isBooleanTrue()) break;
      if (!(whereClause instanceof AndNode)) break;
      AndNode andNode = (AndNode)whereClause;
      whereClause = andNode.getRightOperand();
      ValueNode condition = andNode.getLeftOperand();
      if (m_grouper.getJoinConditions().contains(condition))
        continue;
      switch (condition.getNodeType()) {
      case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
      case NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE:
      case NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE:
      case NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE:
      case NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE:
        break;
      default:
        continue;
      }
      BinaryOperatorNode binop = (BinaryOperatorNode)condition;
      if ((matchColumnReference(column, binop.getLeftOperand()) &&
           (binop.getRightOperand() instanceof ConstantNode)) ||
          (matchColumnReference(column, binop.getRightOperand()) &&
           (binop.getLeftOperand() instanceof ConstantNode))) {
        if (result == null)
          result = new HashSet<BinaryOperatorNode>();
        result.add(binop);
      }
    }
    return result;
  }

  protected static boolean matchColumnReference(Column column, ValueNode operand) {
    if (!(operand instanceof ColumnReference))
      return false;
    ColumnBinding cb = (ColumnBinding)operand.getUserData();
    if (cb == null)
      return false;
    return (column == cb.getColumn());
  }
  
  // TODO: Too much work here dealing with multiple conditions that
  // could have been reconciled earlier as part of normalization.
  protected IndexKeyRange getIndexKeyRange(Index index, 
                                           Set<BinaryOperatorNode> indexConditions) 
      throws StandardException {
    List<IndexColumn> indexColumns = index.getColumns();
    int nkeys = indexColumns.size();
    Object[] keys = new Object[nkeys];
    Object[] lb = null, ub = null;
    boolean lbinc = false, ubinc = false;
    for (int i = 0; i < nkeys; i++) {
      IndexColumn indexColumn = indexColumns.get(i);
      Column column = indexColumn.getColumn();
      Object eqValue = null, ltValue = null, gtValue = null;
      Comparison ltOp = null, gtOp = null;
      for (BinaryOperatorNode condition : indexConditions) {
        boolean reverse;
        Object value;
        if (matchColumnReference(column, condition.getLeftOperand()) &&
            (condition.getRightOperand() instanceof ConstantNode)) {
          value = ((ConstantNode)condition.getRightOperand()).getValue();
          reverse = false;
        }
        else if (matchColumnReference(column, condition.getRightOperand()) &&
                 (condition.getLeftOperand() instanceof ConstantNode)) {
          value = ((ConstantNode)condition.getLeftOperand()).getValue();
          reverse = true;
        }
        else
          continue;
        Comparison op;
        switch (condition.getNodeType()) {
        case NodeTypes.BINARY_EQUALS_OPERATOR_NODE:
          op = Comparison.EQ;
          break;
        case NodeTypes.BINARY_GREATER_THAN_OPERATOR_NODE:
          op = (reverse) ? Comparison.LT : Comparison.GT;
          break;
        case NodeTypes.BINARY_GREATER_EQUALS_OPERATOR_NODE:
          op = (reverse) ? Comparison.LE : Comparison.GE;
          break;
        case NodeTypes.BINARY_LESS_THAN_OPERATOR_NODE:
          op = (reverse) ? Comparison.GT : Comparison.LT;
          break;
        case NodeTypes.BINARY_LESS_EQUALS_OPERATOR_NODE:
          op = (reverse) ? Comparison.GE : Comparison.LE;
          break;
        default:
          continue;
        }
        switch (op) {
        case EQ:
          if (eqValue == null)
            eqValue = value;
          else if (!eqValue.equals(value))
            throw new StandardException("Conflicting equality conditions.");
          break;
        case LT:
        case LE:
          {
            int comp = (ltValue == null) ? +1 : ((Comparable)ltValue).compareTo(value);
            if ((comp > 0) ||
                ((comp == 0) && (op == Comparison.LT) && (ltOp == Comparison.LE))) {
              ltValue = value;
              ltOp = op;
            }
          }
          break;
        case GT:
        case GE:
          {
            int comp = (gtValue == null) ? -1 : ((Comparable)gtValue).compareTo(value);
            if ((comp < 0) ||
                ((comp == 0) && (op == Comparison.GT) && (gtOp == Comparison.GE))) {
              gtValue = value;
              gtOp = op;
            }
          }
          break;
        }
      }
      if (eqValue != null) {
        keys[i] = eqValue;
      }
      else {
        if (gtValue != null) {
          if (lb == null) {
            lb = new Object[nkeys];
            System.arraycopy(keys, 0, lb, 0, nkeys);
          }
          lb[i] = gtValue;
          if (gtOp == Comparison.GE) 
            lbinc = true;
        }
        if (ltValue != null) {
          if (ub == null) {
            ub = new Object[nkeys];
            System.arraycopy(keys, 0, ub, 0, nkeys);
          }
          ub[i] = ltValue;
          if (ltOp == Comparison.LE) 
            ubinc = true;
        }
      }
    }
    if ((lb == null) && (ub == null)) {
      IndexBound eq = getIndexBound(index, keys);
      return new IndexKeyRange(eq, true, eq, true);
    }
    else {
      IndexBound lo = getIndexBound(index, lb);
      IndexBound hi = getIndexBound(index, ub);
      return new IndexKeyRange(lo, lbinc, hi, ubinc);
    }
  }

  static class IndexKeyRow extends RowBase {
    private RowType m_rowType;
    private Object[] m_keys;

    public IndexKeyRow(RowType rowType, Object[] keys) {
      m_rowType = rowType;
      m_keys = keys;
    }

    @Override
    public RowType rowType() {
      return m_rowType;
    }

    @Override
    public Object field(int i) {
      return m_keys[i];
    }

    @Override
    public HKey hKey() {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("<");
      sb.append(m_rowType);
      sb.append(">{");
      for (int i = 0; i < m_keys.length; i++) {
        if (i > 0)
          sb.append(",");
        sb.append(m_keys[i]);
      }
      sb.append("}");
      return sb.toString();
    }
  }

  protected IndexBound getIndexBound(Index index, Object[] keys) {
    if (keys == null) 
      return null;
    IndexKeyType indexKeyType = new IndexKeyType(m_adapter.schema(), index);
    IndexKeyRow row = new IndexKeyRow(m_adapter.schema().indexRowType(index), keys);
    return new IndexBound(indexKeyType, row);
  }

  /**
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
    else if (operator instanceof IndexScan_Default) {
      IndexScan_Default index = (IndexScan_Default)operator;
      into.append("IndexScan_Default(");
      into.append(index.index);
      into.append(")\n");
    }
    else if (operator instanceof IndexLookup_Default) {
      IndexLookup_Default index = (IndexLookup_Default)operator;
      into.append("IndexLookup_Default(");
      into.append(index.groupTable);
      into.append(")\n");
      explainPlan(index.inputOperator, into, depth+1);
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

  protected static String explainBinding(Object binding) {
    if (binding == null)
      return "";
    else if (binding instanceof IndexKeyRange) {
      IndexKeyRange range = (IndexKeyRange)binding;
      IndexBound lo = range.lo();
      IndexBound hi = range.hi();
      boolean loInclusive = range.loInclusive();
      boolean hiInclusive = range.hiInclusive();

      StringBuilder sb = new StringBuilder();
      sb.append(loInclusive ? "(" : "[");
      if (lo != null)
        sb.append(explainBinding(lo));
      sb.append(",");
      if (hi != null)
        sb.append(explainBinding(hi));
      sb.append(hiInclusive ? ")" : "]");
      return sb.toString();
    }
    else if (binding instanceof IndexBound) {
      IndexBound bound = (IndexBound)binding;
      StringBuilder sb = new StringBuilder();
      sb.append("IndexBound(");
      sb.append(bound.indexKeyType);
      sb.append(",");
      sb.append(bound.row);
      sb.append(")");
      return sb.toString();
    }
    else
      return binding.toString();
  }
  **/

}
