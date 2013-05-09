package com.revolsys.gis.data.query;

import com.revolsys.converter.string.StringConverterRegistry;

public class UnaryOperator implements Condition {

  private final Object value;

  private final String operator;

  public UnaryOperator(final String operator, final Object value) {
    this.operator = operator;
    this.value = value;
  }

  public String getOperator() {
    return operator;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public void appendSql(final StringBuffer buffer) {
    buffer.append(operator);
    buffer.append(" ");
    if (value instanceof Condition) {
      final Condition condition = (Condition)value;
      condition.appendSql(buffer);
    } else {
      buffer.append('?');
    }
  }

  @Override
  public String toString() {
    return operator + " " + StringConverterRegistry.toString(value);
  }
}
