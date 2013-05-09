package com.revolsys.gis.data.query;

import com.revolsys.converter.string.StringConverterRegistry;

public class BinaryCondition implements Condition {

  private final String operator;

  private final Object left;

  private final Object right;

  public BinaryCondition(final Object left, final String operator,
    final Object right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    if (left instanceof Condition) {
      final Condition condition = (Condition)left;
      condition.appendSql(buffer);
    } else {
      buffer.append('?');
    }
    buffer.append(" ");
    buffer.append(operator);
    buffer.append(" ");
    if (right instanceof Condition) {
      final Condition condition = (Condition)right;
      condition.appendSql(buffer);
    } else {
      buffer.append('?');
    }
  }

  @Override
  public String toString() {
    return StringConverterRegistry.toString(left) + " " + operator + " "
      + StringConverterRegistry.toString(right);
  }
}
