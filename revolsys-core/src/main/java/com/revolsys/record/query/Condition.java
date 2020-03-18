package com.revolsys.record.query;

import java.util.function.Predicate;

import com.revolsys.record.Record;
import com.revolsys.util.Emptyable;
import com.revolsys.util.Property;

public interface Condition extends QueryValue, Predicate<Record>, Emptyable {

  static final AcceptAllCondition ALL = new AcceptAllCondition();

  default Condition and(final Condition condition) {
    if (Property.isEmpty(condition)) {
      return this;
    } else if (Property.isEmpty(this)) {
      return condition;
    } else {
      return new And(this, condition);
    }
  }

  @Override
  Condition clone();

  @SuppressWarnings("unchecked")
  @Override
  default <V> V getValue(final Record record) {
    final Boolean value = test(record);
    return (V)value;
  }

  @Override
  default boolean isEmpty() {
    return false;
  }

  default Condition not() {
    if (Property.isEmpty(this)) {
      return this;
    } else {
      return new Not(this);
    }
  }

  default Condition or(final Condition condition) {
    if (Property.isEmpty(condition)) {
      return this;
    } else if (Property.isEmpty(this)) {
      return condition;
    } else {
      return new Or(this, condition);
    }
  }

  @Override
  default boolean test(final Record record) {
    throw new UnsupportedOperationException("Cannot filter using " + toString());
  }

  @Override
  default String toFormattedString() {
    return toString();
  }
}
