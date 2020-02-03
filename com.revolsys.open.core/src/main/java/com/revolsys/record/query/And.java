package com.revolsys.record.query;

import java.util.Arrays;

import com.revolsys.record.Record;
import com.revolsys.util.Property;

public class And extends AbstractMultiCondition {

  public And(final Condition... conditions) {
    this(Arrays.asList(conditions));
  }

  public And(final Iterable<? extends Condition> conditions) {
    super("AND", conditions);
  }

  @Override
  public And and(final Condition condition) {
    if (!Property.isEmpty(condition)) {
      addCondition(condition);
    }
    return this;
  }

  @Override
  public And clone() {
    return (And)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof And) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public boolean test(final Record record) {
    final QueryValue[] values = this.values;
    if (values.length > 0) {
      for (final QueryValue value : values) {
        final Condition condition = (Condition)value;
        if (!condition.test(record)) {
          return false;
        }
      }
    }
    return true;
  }
}
