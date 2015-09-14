package com.revolsys.record.query;

import java.util.Map;
import java.util.function.Predicate;

public abstract class Condition extends QueryValue implements Predicate<Map<String, Object>> {

  public static boolean test(final Predicate<Map<String, Object>> condition,
    final Map<String, Object> record) {
    if (condition == null) {
      return true;
    } else {
      return condition.test(record);
    }
  }

  public And and(final Condition condition) {
    return new And(this, condition);
  }

  @Override
  public Condition clone() {
    return (Condition)super.clone();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Map<String, Object> record) {
    final Boolean value = test(record);
    return (V)value;
  }

  public boolean isEmpty() {
    return false;
  }

  public Or or(final Condition condition) {
    return new Or(this, condition);
  }

  @Override
  public boolean test(final Map<String, Object> record) {
    throw new UnsupportedOperationException("Cannot filter using " + toString());
  }

  @Override
  public String toFormattedString() {
    return toString();
  }
}
