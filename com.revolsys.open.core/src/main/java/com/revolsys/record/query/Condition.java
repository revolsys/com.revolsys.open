package com.revolsys.record.query;

import java.util.function.Predicate;

import com.revolsys.record.Record;

public abstract class Condition extends QueryValue implements Predicate<Record> {
  public static final AcceptAllCondition ALL = new AcceptAllCondition();

  public Condition and(final Condition condition) {
    return new And(this, condition);
  }

  @Override
  public Condition clone() {
    return (Condition)super.clone();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Record record) {
    final Boolean value = test(record);
    return (V)value;
  }

  public boolean isEmpty() {
    return false;
  }

  public Condition or(final Condition condition) {
    return new Or(this, condition);
  }

  @Override
  public boolean test(final Record record) {
    throw new UnsupportedOperationException("Cannot filter using " + toString());
  }

  @Override
  public String toFormattedString() {
    return toString();
  }
}
