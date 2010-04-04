package com.revolsys.jump.feature.filter.operator;

public class EqualsOperator implements Operator {

  public String getSymbol() {
    return "=";
  }

  public boolean match(final Object value1, final Object value2) {
    if (value1 == value2) {
      return true;
    } else if (value1 == null) {
      return value2.toString().trim().length() == 0;
    } else if (value2 == null) {
      return value1.toString().trim().length() == 0;
    } else if (value1.equals(value2)) {
      return true;
    } else {
      return value1.toString().equals(value2.toString());
    }
  }

  public String toString() {
    return getSymbol();
  }
}
