package com.revolsys.jump.feature.filter.operator;

public interface Operator {
  boolean match(Object value1, Object value2);

  String getSymbol();
}
