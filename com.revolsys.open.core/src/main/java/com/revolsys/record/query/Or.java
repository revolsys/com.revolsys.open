package com.revolsys.record.query;

import java.util.Arrays;
import java.util.List;

import com.revolsys.record.Record;
import com.revolsys.util.Property;

public class Or extends AbstractMultiCondition {

  public Or(final Condition... conditions) {
    this(Arrays.asList(conditions));
  }

  public Or(final Iterable<? extends Condition> conditions) {
    super("OR", conditions);
  }

  @Override
  public Or clone() {
    return (Or)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Or) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public Or or(final Condition condition) {
    if (!Property.isEmpty(condition)) {
      add(condition);
    }
    return this;
  }

  @Override
  public boolean test(final Record object) {
    final List<Condition> conditions = (List)getQueryValues();
    if (conditions.isEmpty()) {
      return true;
    } else {
      for (final Condition condition : conditions) {
        if (condition.test(object)) {
          return true;
        }
      }
      return false;
    }
  }
}
