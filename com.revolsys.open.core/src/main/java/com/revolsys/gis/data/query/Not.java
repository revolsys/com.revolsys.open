package com.revolsys.gis.data.query;

import java.util.Map;

public class Not extends LeftUnaryCondition {

  public Not(final Condition condition) {
    super("NOT", condition);
  }

  @Override
  public boolean accept(final Map<String, Object> object) {
    final Condition condition = getQueryValue();
    if (!condition.accept(object)) {
      return false;
    }
    return true;
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
}
