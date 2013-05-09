package com.revolsys.gis.data.query;

import java.util.Arrays;

public class Conditions {

  public static BinaryCondition between(final Object value1, final Object value2) {
    return new BinaryCondition(value1, "BETWEEN", value2);
  }

  public static BinaryCondition equal(final Object left, final Object right) {
    return new BinaryCondition(left, "=", right);
  }

  public static BinaryCondition greaterThan(final Object left,
    final Object right) {
    return new BinaryCondition(left, ">", right);
  }

  public static BinaryCondition greaterThanOrEqual(final Object left,
    final Object right) {
    return new BinaryCondition(left, ">=", right);
  }

  public static BinaryCondition in(final Object left, final Object... right) {
    return new BinaryCondition(left, "IN", Arrays.asList(right));
  }

  public static Condition lessThan(final Object left, final Object right) {
    return new BinaryCondition(left, "<", right);
  }

  public static Condition lessThanOrEqual(final Object left, final Object right) {
    return new BinaryCondition(left, "<=", right);
  }

  public static Condition like(final Object left, final Object right) {
    return new BinaryCondition(left, "LIKE", right);
  }

  public static Condition notEqual(final Object left, final Object right) {
    return new BinaryCondition(left, "<>", right);
  }

  public static UnaryOperator not(final Object value) {
    return new UnaryOperator("NOT", value);
  }

}
