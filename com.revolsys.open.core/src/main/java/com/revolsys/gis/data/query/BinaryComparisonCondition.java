package com.revolsys.gis.data.query;

public class BinaryComparisonCondition implements Condition {

  public static Condition equal(final Object left, final Object right) {
    return new BinaryComparisonCondition(left, "=", right);
  }

  public static Condition greaterThan(final Object left, final Object right) {
    return new BinaryComparisonCondition(left, ">", right);
  }

  public static Condition greaterThanOrEqual(final Object left,
    final Object right) {
    return new BinaryComparisonCondition(left, ">=", right);
  }

  public static Condition lessThan(final Object left, final Object right) {
    return new BinaryComparisonCondition(left, "<", right);
  }

  public static Condition lessThanOrEqual(final Object left, final Object right) {
    return new BinaryComparisonCondition(left, "<=", right);
  }

  public static Condition like(final Object left, final Object right) {
    return new BinaryComparisonCondition(left, "LIKE", right);
  }

  public static Condition notEqual(final Object left, final Object right) {
    return new BinaryComparisonCondition(left, "<>", right);
  }

  private final String operator;

  private final Object left;

  private final Object right;

  public BinaryComparisonCondition(final Object left, final String operator,
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
}
