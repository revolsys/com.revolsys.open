package com.revolsys.gis.data.query;

public class BinaryComparisonCondition implements Condition {

  public static Condition equal(Object left, Object right) {
    return new BinaryComparisonCondition(left, "=", right);
  }

  public static Condition notEqual(Object left, Object right) {
    return new BinaryComparisonCondition(left, "<>", right);
  }

  public static Condition lessThan(Object left, Object right) {
    return new BinaryComparisonCondition(left, "<", right);
  }

  public static Condition lessThanOrEqual(Object left, Object right) {
    return new BinaryComparisonCondition(left, "<=", right);
  }

  public static Condition greaterThan(Object left, Object right) {
    return new BinaryComparisonCondition(left, ">", right);
  }

  public static Condition greaterThanOrEqual(Object left, Object right) {
    return new BinaryComparisonCondition(left, ">=", right);
  }

  public static Condition like(Object left, Object right) {
    return new BinaryComparisonCondition(left, "LIKE", right);
  }

  private String operator;

  private Object left;

  private Object right;

  public BinaryComparisonCondition(Object left, String operator, Object right) {
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public void appendSql(StringBuffer buffer) {
    if (left instanceof Condition) {
      Condition condition = (Condition)left;
      condition.appendSql(buffer);
    } else {
      buffer.append('?');
    }
    buffer.append(" ");
    buffer.append(operator);
    buffer.append(" ");
    if (right instanceof Condition) {
      Condition condition = (Condition)right;
      condition.appendSql(buffer);
    } else {
      buffer.append('?');
    }
  }
}
