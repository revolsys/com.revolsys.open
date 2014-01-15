package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.akiban.sql.parser.BetweenOperatorNode;
import com.akiban.sql.parser.BinaryArithmeticOperatorNode;
import com.akiban.sql.parser.BinaryLogicalOperatorNode;
import com.akiban.sql.parser.BinaryOperatorNode;
import com.akiban.sql.parser.CastNode;
import com.akiban.sql.parser.ColumnReference;
import com.akiban.sql.parser.ConstantNode;
import com.akiban.sql.parser.CursorNode;
import com.akiban.sql.parser.InListOperatorNode;
import com.akiban.sql.parser.IsNullNode;
import com.akiban.sql.parser.LikeEscapeOperatorNode;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.NotNode;
import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.RowConstructorNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.SimpleStringOperatorNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.UserTypeConstantNode;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.parser.ValueNodeList;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.util.ExceptionUtil;

public abstract class QueryValue implements Cloneable {
  /** Must be in upper case */
  public static final List<String> SUPPORTED_BINARY_OPERATORS = Arrays.asList(
    "AND", "OR", "+", "-", "/", "*", "=", "<>", "<", "<=", ">", ">=", "LIKE",
    "+", "-", "/", "*", "%", "MOD");

  public static <V extends QueryValue> List<V> cloneQueryValues(
    final List<V> values) {
    final List<V> clonedValues = new ArrayList<V>();
    for (final V value : values) {
      @SuppressWarnings("unchecked")
      final V clonedValue = (V)value.clone();
      clonedValues.add(clonedValue);
    }
    return clonedValues;
  }

  public static Condition parseWhere(final DataObjectMetaData metaData,
    final String whereClause) {
    try {
      final StatementNode statement = new SQLParser().parseStatement("SELECT * FROM "
        + metaData.getTypeName() + " WHERE " + whereClause);
      if (statement instanceof CursorNode) {
        final CursorNode selectStatement = (CursorNode)statement;
        final ResultSetNode resultSetNode = selectStatement.getResultSetNode();
        if (resultSetNode instanceof SelectNode) {
          final SelectNode selectNode = (SelectNode)resultSetNode;
          final ValueNode where = selectNode.getWhereClause();
          final Condition condition = toQueryValue(metaData, where);
          return condition;
        }
      }
      return null;
    } catch (final Throwable e) {
      throw new IllegalArgumentException(
        "Invalid where clause: " + whereClause, e);
    }
  }

  @SuppressWarnings("unchecked")
  public static <V extends QueryValue> V toQueryValue(
    final DataObjectMetaData metaData, final ValueNode expression) {
    if (expression instanceof BetweenOperatorNode) {
      final BetweenOperatorNode betweenExpression = (BetweenOperatorNode)expression;
      final ValueNode leftValueNode = betweenExpression.getLeftOperand();
      final ValueNodeList rightOperandList = betweenExpression.getRightOperandList();
      final ValueNode betweenExpressionStart = rightOperandList.get(0);
      final ValueNode betweenExpressionEnd = rightOperandList.get(1);
      if (!(leftValueNode instanceof ColumnReference)) {
        throw new IllegalArgumentException(
          "Between operator must use a column name not: " + leftValueNode);
      }

      if (!(betweenExpressionStart instanceof NumericConstantNode)) {
        throw new IllegalArgumentException(
          "Between min value must be a number not: " + betweenExpressionStart);
      }
      if (!(betweenExpressionEnd instanceof NumericConstantNode)) {
        throw new IllegalArgumentException(
          "Between max value must be a number not: " + betweenExpressionEnd);
      }
      final Column column = toQueryValue(metaData, leftValueNode);
      final Value min = toQueryValue(metaData, betweenExpressionStart);
      final Value max = toQueryValue(metaData, betweenExpressionEnd);
      final Attribute attribute = metaData.getAttribute(column.getName());
      min.convert(attribute);
      max.convert(attribute);
      return (V)new Between(column, min, max);
    } else if (expression instanceof BinaryLogicalOperatorNode) {
      final BinaryLogicalOperatorNode binaryOperatorNode = (BinaryLogicalOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator().toUpperCase();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      final Condition leftCondition = toQueryValue(metaData, leftValueNode);
      final Condition rightCondition = toQueryValue(metaData, rightValueNode);
      if ("AND".equals(operator)) {
        return (V)new And(leftCondition, rightCondition);
      } else if ("OR".equals(operator)) {
        return (V)new Or(leftCondition, rightCondition);
      } else {
        throw new IllegalArgumentException("Binary logical operator "
          + operator + " not supported.");
      }
    } else if (expression instanceof BinaryOperatorNode) {
      final BinaryOperatorNode binaryOperatorNode = (BinaryOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      if (SUPPORTED_BINARY_OPERATORS.contains(operator.toUpperCase())) {
        final QueryValue leftCondition = toQueryValue(metaData, leftValueNode);
        QueryValue rightCondition = toQueryValue(metaData, rightValueNode);

        if (leftCondition instanceof Column) {
          if (rightCondition instanceof Value) {
            final Object value = ((Value)rightCondition).getValue();
            if (value == null) {
              throw new IllegalArgumentException("Values can't be null for "
                + operator + " use IS NULL or IS NOT NULL instead.");
            } else {
              final Column column = (Column)leftCondition;
              final Attribute attribute = metaData.getAttribute(column.getName());
              final Class<?> typeClass = attribute.getTypeClass();
              try {
                final Object convertedValue = StringConverterRegistry.toObject(
                  typeClass, value);
                if (convertedValue == null
                  || !typeClass.isAssignableFrom(typeClass)) {
                  throw new IllegalArgumentException(column.getName()
                    + " requires a " + attribute.getType() + " not the value "
                    + value);
                } else {
                  rightCondition = new Value(convertedValue);
                }
              } catch (final Throwable t) {
                throw new IllegalArgumentException(column.getName()
                  + " requires a " + attribute.getType() + " not the value "
                  + value);
              }
            }
          }
        }
        if (expression instanceof BinaryArithmeticOperatorNode) {
          final QueryValue arithmaticCondition = Q.arithmatic(leftCondition,
            operator, rightCondition);
          return (V)arithmaticCondition;
        } else {
          final Condition binaryCondition = Q.binary(leftCondition, operator,
            rightCondition);
          return (V)binaryCondition;
        }
      } else {
        throw new IllegalArgumentException("Unsupported binary operator "
          + operator);
      }
    } else if (expression instanceof ColumnReference) {
      final ColumnReference column = (ColumnReference)expression;
      String columnName = column.getColumnName();
      columnName = columnName.replaceAll("\"", "");
      final Attribute attribute = metaData.getAttribute(columnName);
      if (attribute == null) {
        throw new IllegalArgumentException("Invalid column name " + columnName);
      } else {
        return (V)new Column(attribute);
      }
    } else if (expression instanceof LikeEscapeOperatorNode) {
      final LikeEscapeOperatorNode likeEscapeOperatorNode = (LikeEscapeOperatorNode)expression;
      final ValueNode leftValueNode = likeEscapeOperatorNode.getReceiver();
      final ValueNode rightValueNode = likeEscapeOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(metaData, leftValueNode);
      final QueryValue rightCondition = toQueryValue(metaData, rightValueNode);
      return (V)new ILike(leftCondition, rightCondition);
    } else if (expression instanceof NotNode) {
      final NotNode notNode = (NotNode)expression;
      final ValueNode operand = notNode.getOperand();
      final Condition condition = toQueryValue(metaData, operand);
      return (V)new Not(condition);
    } else if (expression instanceof InListOperatorNode) {
      final InListOperatorNode inListOperatorNode = (InListOperatorNode)expression;
      final ValueNode leftOperand = inListOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(metaData, leftOperand);

      final List<QueryValue> conditions = new ArrayList<QueryValue>();
      final RowConstructorNode itemsList = inListOperatorNode.getRightOperandList();
      for (final ValueNode itemValueNode : itemsList.getNodeList()) {
        final QueryValue itemCondition = toQueryValue(metaData, itemValueNode);
        conditions.add(itemCondition);
      }
      return (V)new In(leftCondition, new CollectionValue(conditions));
    } else if (expression instanceof IsNullNode) {
      final IsNullNode isNullNode = (IsNullNode)expression;
      final ValueNode operand = isNullNode.getOperand();
      final QueryValue value = toQueryValue(metaData, operand);
      if (isNullNode.getNodeType() == NodeTypes.IS_NOT_NULL_NODE) {
        return (V)new IsNotNull(value);
      } else {
        return (V)new IsNull(value);
      }
      // } else if (expression instanceof Parenthesis) {
      // final Parenthesis parenthesis = (Parenthesis)expression;
      // final ValueNode parenthesisValueNode = parenthesis.getExpression();
      // final Condition condition = toCondition(parenthesisExpression);
      // final ParenthesisCondition parenthesisCondition = new
      // ParenthesisCondition(
      // condition);
      // if (parenthesis.isNot()) {
      // return (V)Q.not(parenthesisCondition);
      // } else {
      // return (V)parenthesisCondition;
      // }
    } else if (expression instanceof RowConstructorNode) {
      final RowConstructorNode rowConstructorNode = (RowConstructorNode)expression;
      final ValueNodeList values = rowConstructorNode.getNodeList();
      final ValueNode valueNode = values.get(0);
      return (V)toQueryValue(metaData, valueNode);
    } else if (expression instanceof UserTypeConstantNode) {
      final UserTypeConstantNode constant = (UserTypeConstantNode)expression;
      final Object objectValue = constant.getObjectValue();
      return (V)new Value(objectValue);
    } else if (expression instanceof ConstantNode) {
      final ConstantNode constant = (ConstantNode)expression;
      final Object value = constant.getValue();
      return (V)new Value(value);
    } else if (expression instanceof SimpleStringOperatorNode) {
      final SimpleStringOperatorNode operatorNode = (SimpleStringOperatorNode)expression;
      final String functionName = operatorNode.getMethodName().toUpperCase();
      final ValueNode operand = operatorNode.getOperand();
      final QueryValue condition = toQueryValue(metaData, operand);
      return (V)new Function(functionName, condition);
    } else if (expression instanceof CastNode) {
      final CastNode castNode = (CastNode)expression;
      final String typeName = castNode.getType().getSQLstring();
      final ValueNode operand = castNode.getCastOperand();
      final QueryValue condition = toQueryValue(metaData, operand);
      return (V)new Cast(condition, typeName);
    } else if (expression == null) {
      return null;
    } else {
      throw new IllegalArgumentException("Unsupported expression"
        + expression.getClass() + " " + expression);
    }
  }

  // TODO wrap in a more generic structure
  public abstract int appendParameters(int index, PreparedStatement statement);

  public abstract void appendSql(StringBuffer buffer);

  @Override
  public QueryValue clone() {
    try {
      final QueryValue clone = (QueryValue)super.clone();
      return clone;
    } catch (final CloneNotSupportedException e) {
      ExceptionUtil.throwUncheckedException(e);
      return null;
    }
  }

  public List<QueryValue> getQueryValues() {
    return Collections.emptyList();
  }

  public String getStringValue(final Map<String, Object> record) {
    final Object value = getValue(record);
    return StringConverterRegistry.toString(value);
  }

  public abstract <V> V getValue(Map<String, Object> record);

  public String toFormattedString() {
    return toString();
  }

}
