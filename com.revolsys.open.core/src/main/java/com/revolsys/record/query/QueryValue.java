package com.revolsys.record.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jeometry.common.data.type.DataTypes;

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
import com.akiban.sql.parser.JavaToSQLValueNode;
import com.akiban.sql.parser.JavaValueNode;
import com.akiban.sql.parser.LikeEscapeOperatorNode;
import com.akiban.sql.parser.NodeTypes;
import com.akiban.sql.parser.NotNode;
import com.akiban.sql.parser.NumericConstantNode;
import com.akiban.sql.parser.ResultSetNode;
import com.akiban.sql.parser.RowConstructorNode;
import com.akiban.sql.parser.SQLParser;
import com.akiban.sql.parser.SQLToJavaValueNode;
import com.akiban.sql.parser.SelectNode;
import com.akiban.sql.parser.SimpleStringOperatorNode;
import com.akiban.sql.parser.StatementNode;
import com.akiban.sql.parser.StaticMethodCallNode;
import com.akiban.sql.parser.UserTypeConstantNode;
import com.akiban.sql.parser.ValueNode;
import com.akiban.sql.parser.ValueNodeList;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.editor.BoundingBoxEditor;
import com.revolsys.record.Record;
import com.revolsys.record.code.CodeTable;
import com.revolsys.record.query.functions.EnvelopeIntersects;
import com.revolsys.record.query.functions.Function;
import com.revolsys.record.query.functions.GetMapValue;
import com.revolsys.record.query.functions.WithinDistance;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.record.schema.RecordStore;
import com.revolsys.util.Property;

public interface QueryValue extends Cloneable {
  /** Must be in upper case */
  static final List<String> SUPPORTED_BINARY_OPERATORS = Arrays.asList("AND", "OR", "+", "-", "/",
    "*", "=", "<>", "<", "<=", ">", ">=", "LIKE", "+", "-", "/", "*", "%", "MOD");

  static <V extends QueryValue> List<V> cloneQueryValues(final List<V> values) {
    final List<V> clonedValues = new ArrayList<>();
    for (final QueryValue value : values) {
      if (value == null) {
        clonedValues.add(null);
      } else {
        @SuppressWarnings("unchecked")
        final V clonedValue = (V)value.clone();
        clonedValues.add(clonedValue);
      }
    }
    return clonedValues;
  }

  static QueryValue[] cloneQueryValues(final QueryValue[] oldValues) {
    if (oldValues == null || oldValues.length == 0) {
      return oldValues;
    } else {
      final QueryValue[] clonedValues = new QueryValue[oldValues.length];
      for (int i = 0; i < oldValues.length; i++) {
        final QueryValue value = oldValues[i];
        clonedValues[i] = value.clone();
      }
      return clonedValues;
    }
  }

  static BoundingBox getBoundingBox(final Query query) {
    final Condition whereCondition = query.getWhereCondition();
    return getBoundingBox(whereCondition);
  }

  static BoundingBox getBoundingBox(final QueryValue queryValue) {
    boolean hasBbox = false;
    final BoundingBoxEditor boundingBox = new BoundingBoxEditor();
    if (queryValue != null) {
      for (final QueryValue childValue : queryValue.getQueryValues()) {
        if (childValue instanceof EnvelopeIntersects) {
          final EnvelopeIntersects intersects = (EnvelopeIntersects)childValue;
          final BoundingBox boundingBox1 = getBoundingBox(intersects.getBoundingBox1Value());
          final BoundingBox boundingBox2 = getBoundingBox(intersects.getBoundingBox2Value());
          hasBbox = true;
          boundingBox.addAllBbox(boundingBox1, boundingBox2);
        } else if (childValue instanceof WithinDistance) {
          final WithinDistance withinDistance = (WithinDistance)childValue;
          final BoundingBox boundingBox1 = getBoundingBox(withinDistance.getGeometry1Value());
          final BoundingBox boundingBox2 = getBoundingBox(withinDistance.getGeometry2Value());
          final BoundingBoxEditor withinBoundingBox = BoundingBox.bboxEditor(boundingBox1,
            boundingBox2);
          final double distance = ((Number)((Value)withinDistance.getDistanceValue()).getValue())
            .doubleValue();

          hasBbox = true;
          boundingBox.addBbox(withinBoundingBox.expandDelta(distance));
        } else if (childValue instanceof Value) {
          final Value valueContainer = (Value)childValue;
          final Object value = valueContainer.getValue();
          if (value instanceof BoundingBoxProxy) {
            hasBbox = true;
            boundingBox.addBbox((BoundingBox)value);
          }
        }
      }
    }
    if (hasBbox) {
      return boundingBox;
    } else {
      return null;
    }
  }

  static Condition parseWhere(final RecordDefinition recordDefinition, final String whereClause) {
    if (Property.hasValue(whereClause)) {
      try {
        final SQLParser sqlParser = new SQLParser();
        String name;
        if (recordDefinition == null) {
          name = "Unknown";
        } else {
          name = recordDefinition.getName();
        }
        final String query = "SELECT * FROM \"" + name + "\" WHERE " + whereClause;
        final StatementNode statement = sqlParser.parseStatement(query);
        if (statement instanceof CursorNode) {
          final CursorNode selectStatement = (CursorNode)statement;
          final ResultSetNode resultSetNode = selectStatement.getResultSetNode();
          if (resultSetNode instanceof SelectNode) {
            final SelectNode selectNode = (SelectNode)resultSetNode;
            final ValueNode where = selectNode.getWhereClause();
            final Condition condition = toQueryValue(recordDefinition, where);
            return condition;
          }
        }
        return null;
      } catch (final Throwable e) {
        throw new IllegalArgumentException("Invalid where clause: " + whereClause, e);
      }
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  static <V extends QueryValue> V toQueryValue(final RecordDefinition recordDefinition,
    final ValueNode expression) {
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
      final Column column = toQueryValue(recordDefinition, leftValueNode);
      final Value min = toQueryValue(recordDefinition, betweenExpressionStart);
      final Value max = toQueryValue(recordDefinition, betweenExpressionEnd);
      if (recordDefinition != null) {
        final FieldDefinition field = recordDefinition.getField(column.getName());
        min.convert(field);
        max.convert(field);
      }
      return (V)new Between(column, min, max);
    } else if (expression instanceof BinaryLogicalOperatorNode) {
      final BinaryLogicalOperatorNode binaryOperatorNode = (BinaryLogicalOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator().toUpperCase();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      final Condition leftCondition = toQueryValue(recordDefinition, leftValueNode);
      final Condition rightCondition = toQueryValue(recordDefinition, rightValueNode);
      if ("AND".equals(operator)) {
        return (V)new And(leftCondition, rightCondition);
      } else if ("OR".equals(operator)) {
        return (V)new Or(leftCondition, rightCondition);
      } else {
        throw new IllegalArgumentException(
          "Binary logical operator " + operator + " not supported.");
      }
    } else if (expression instanceof BinaryOperatorNode) {
      final BinaryOperatorNode binaryOperatorNode = (BinaryOperatorNode)expression;
      final String operator = binaryOperatorNode.getOperator();
      final ValueNode leftValueNode = binaryOperatorNode.getLeftOperand();
      final ValueNode rightValueNode = binaryOperatorNode.getRightOperand();
      if (SUPPORTED_BINARY_OPERATORS.contains(operator.toUpperCase())) {
        final QueryValue leftCondition = toQueryValue(recordDefinition, leftValueNode);
        QueryValue rightCondition = toQueryValue(recordDefinition, rightValueNode);

        if (leftCondition instanceof Column) {
          if (rightCondition instanceof Value) {
            final Column column = (Column)leftCondition;

            final String name = column.getName();
            final Object value = ((Value)rightCondition).getValue();
            if (value == null) {
              throw new IllegalArgumentException(
                "Values can't be null for " + operator + " use IS NULL or IS NOT NULL instead.");
            } else if (recordDefinition != null) {
              final FieldDefinition fieldDefinition = recordDefinition.getField(name);
              final CodeTable codeTable = recordDefinition.getCodeTableByFieldName(name);
              if (codeTable == null || fieldDefinition == recordDefinition.getIdField()) {
                final Object convertedValue = fieldDefinition.toFieldValueException(value);
                if (convertedValue == null) {
                  throw new IllegalArgumentException("Values can't be null for " + operator
                    + " use IS NULL or IS NOT NULL instead.");
                } else {
                  rightCondition = new Value(fieldDefinition, convertedValue);
                }
              } else {
                Object id;
                if (value instanceof String) {
                  final String string = (String)value;
                  final String[] values = string.split(":");
                  id = codeTable.getIdentifier((Object[])values);
                } else {
                  id = codeTable.getIdentifier(value);
                }
                if (id == null) {
                  throw new IllegalArgumentException(name + "='" + value
                    + "' could not be found in the code table " + codeTable.getName());
                } else {
                  rightCondition = new Value(fieldDefinition, id);
                }
              }
            }
          }
        }
        if (expression instanceof BinaryArithmeticOperatorNode) {
          final QueryValue arithmaticCondition = Q.arithmatic(leftCondition, operator,
            rightCondition);
          return (V)arithmaticCondition;
        } else {
          final Condition binaryCondition = Q.binary(leftCondition, operator, rightCondition);
          return (V)binaryCondition;
        }
      } else {
        throw new IllegalArgumentException("Unsupported binary operator " + operator);
      }
    } else if (expression instanceof ColumnReference) {
      final ColumnReference column = (ColumnReference)expression;
      String columnName = column.getColumnName();
      columnName = columnName.replaceAll("\"", "");
      if (recordDefinition == null) {
        return (V)new Column(columnName);
      } else {
        final FieldDefinition fieldDefinition = recordDefinition.getField(columnName);
        if (fieldDefinition == null) {
          recordDefinition.getField(columnName);
          throw new IllegalArgumentException("Invalid column name " + columnName);
        } else {
          return (V)new Column(fieldDefinition);
        }
      }
    } else if (expression instanceof LikeEscapeOperatorNode) {
      final LikeEscapeOperatorNode likeEscapeOperatorNode = (LikeEscapeOperatorNode)expression;
      final ValueNode leftValueNode = likeEscapeOperatorNode.getReceiver();
      final ValueNode rightValueNode = likeEscapeOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(recordDefinition, leftValueNode);
      final QueryValue rightCondition = toQueryValue(recordDefinition, rightValueNode);
      return (V)new ILike(leftCondition, rightCondition);
    } else if (expression instanceof NotNode) {
      final NotNode notNode = (NotNode)expression;
      final ValueNode operand = notNode.getOperand();
      final Condition condition = toQueryValue(recordDefinition, operand);
      return (V)new Not(condition);
    } else if (expression instanceof InListOperatorNode) {
      final InListOperatorNode inListOperatorNode = (InListOperatorNode)expression;
      final ValueNode leftOperand = inListOperatorNode.getLeftOperand();
      final QueryValue leftCondition = toQueryValue(recordDefinition, leftOperand);

      final List<QueryValue> conditions = new ArrayList<>();
      final RowConstructorNode itemsList = inListOperatorNode.getRightOperandList();
      for (final ValueNode itemValueNode : itemsList.getNodeList()) {
        final QueryValue itemCondition = toQueryValue(recordDefinition, itemValueNode);
        conditions.add(itemCondition);
      }
      return (V)new In(leftCondition, new CollectionValue(conditions));
    } else if (expression instanceof IsNullNode) {
      final IsNullNode isNullNode = (IsNullNode)expression;
      final ValueNode operand = isNullNode.getOperand();
      final QueryValue value = toQueryValue(recordDefinition, operand);
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
      return (V)toQueryValue(recordDefinition, valueNode);
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
      final QueryValue condition = toQueryValue(recordDefinition, operand);
      return (V)new Function(functionName, condition);
    } else if (expression instanceof CastNode) {
      final CastNode castNode = (CastNode)expression;
      final String typeName = castNode.getType().getSQLstring();
      final ValueNode operand = castNode.getCastOperand();
      final QueryValue condition = toQueryValue(recordDefinition, operand);
      return (V)new Cast(condition, typeName);
    } else if (expression instanceof JavaToSQLValueNode) {
      final JavaToSQLValueNode node = (JavaToSQLValueNode)expression;
      final JavaValueNode javaValueNode = node.getJavaValueNode();
      if (javaValueNode instanceof StaticMethodCallNode) {
        final StaticMethodCallNode methodNode = (StaticMethodCallNode)javaValueNode;
        final List<QueryValue> parameters = new ArrayList<>();

        final String methodName = methodNode.getMethodName();
        for (final JavaValueNode parameter : methodNode.getMethodParameters()) {
          if (parameter instanceof SQLToJavaValueNode) {
            final SQLToJavaValueNode sqlNode = (SQLToJavaValueNode)parameter;
            final QueryValue param = toQueryValue(recordDefinition, sqlNode.getSQLValueNode());
            parameters.add(param);
          }
        }
        if (methodName.equals("get_map_value")) {
          return (V)new GetMapValue(parameters);
        }

      }
      return null;
    } else if (expression == null) {
      return null;
    } else {
      throw new IllegalArgumentException(
        "Unsupported expression" + expression.getClass() + " " + expression);
    }
  }

  void appendDefaultSql(Query query, RecordStore recordStore, StringBuilder sql);

  // TODO wrap in a more generic structure
  int appendParameters(int index, PreparedStatement statement);

  default void appendSql(final Query query, final RecordStore recordStore,
    final StringBuilder sql) {
    if (recordStore == null) {
      appendDefaultSql(query, null, sql);
    } else {
      recordStore.appendQueryValue(query, sql, this);
    }
  }

  QueryValue clone();

  default List<QueryValue> getQueryValues() {
    return Collections.emptyList();
  }

  default String getStringValue(final Record record) {
    final Object value = getValue(record);
    return DataTypes.toString(value);
  }

  <V> V getValue(Record record);

  default void setFieldDefinition(final FieldDefinition fieldDefinition) {
  }

  default void setRecordDefinition(final RecordDefinition recordDefinition) {
    for (final QueryValue queryValue : getQueryValues()) {
      if (queryValue != null) {
        queryValue.setRecordDefinition(recordDefinition);
      }
    }
  }

  default String toFormattedString() {
    return toString();
  }

  @SuppressWarnings("unchecked")
  default <QV extends QueryValue> QV updateQueryValues(
    final java.util.function.Function<QueryValue, QueryValue> valueHandler) {
    return (QV)this;
  }

}
