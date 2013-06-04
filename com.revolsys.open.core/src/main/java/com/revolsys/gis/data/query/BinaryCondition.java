package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;

public class BinaryCondition extends AbstractCondition {

  private final String operator;

  private final Condition left;

  private final Condition right;

  public BinaryCondition(final Condition left, final String operator,
    final Condition right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public BinaryCondition(final String name, final String operator,
    final Object value) {
    this(new Column(name), operator, new Value(value));
  }

  @Override
  public int appendParameters(int index, final PreparedStatement statement) {
    index = left.appendParameters(index, statement);
    index = right.appendParameters(index, statement);
    return index;
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    left.appendSql(buffer);
    buffer.append(" ");
    buffer.append(operator);
    buffer.append(" ");
    right.appendSql(buffer);
  }

  @Override
  public BinaryCondition clone() {
    return new BinaryCondition(left.clone(), operator, right.clone());
  }

  @Override
  public List<Condition> getConditions() {
    return Arrays.asList(left, right);
  }

  public Condition getLeft() {
    return left;
  }

  public String getOperator() {
    return operator;
  }

  public Condition getRight() {
    return right;
  }

  @Override
  public String toString() {
    return StringConverterRegistry.toString(left) + " " + operator + " "
      + StringConverterRegistry.toString(right);
  }
}
