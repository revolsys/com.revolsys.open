package com.revolsys.data.query;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Or extends AbstractMultiCondition {

  public Or(final Collection<? extends Condition> conditions) {
    super("OR", conditions);
  }

  public Or(final Condition... conditions) {
    this(Arrays.asList(conditions));
  }

  @Override
  public boolean test(final Map<String, Object> object) {
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
}
