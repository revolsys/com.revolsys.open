package com.revolsys.record.query;

import java.util.Arrays;
import java.util.List;

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
      add(condition);
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

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public boolean test(final Record object) {
    final List<Condition> conditions = (List)getQueryValues();
    for (final Condition condition : conditions) {
      if (!condition.test(object)) {
        return false;
      }
    }
    return true;
  }
}
