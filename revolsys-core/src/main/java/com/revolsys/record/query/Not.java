package com.revolsys.record.query;

import com.revolsys.record.Record;

public class Not extends LeftUnaryCondition {

  public Not(final Condition condition) {
    super("NOT", condition);
  }

  @Override
  public Not clone() {
    return (Not)super.clone();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof Not) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public boolean test(final Record object) {
    final Condition condition = getValue();
    if (condition.test(object)) {
      return false;
    } else {
      return true;
    }
  }
}
