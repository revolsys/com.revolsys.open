package com.revolsys.jump.feature.filter.operator;

public class NotEqualsOperator extends EqualsOperator {
  public String getSymbol() {
    return "!=";
  }

  public boolean match(final Object value1, final Object value2) {
    boolean match = !super.match(value1, value2);
    return match;
  }
}
