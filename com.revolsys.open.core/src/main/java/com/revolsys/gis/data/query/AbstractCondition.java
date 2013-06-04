package com.revolsys.gis.data.query;

import java.util.Collections;
import java.util.List;

public abstract class AbstractCondition implements Condition {

  @Override
  public Condition clone() {
    try {
      return (Condition)super.clone();
    } catch (final CloneNotSupportedException e) {
      return null;
    }
  }

  @Override
  public List<Condition> getConditions() {
    return Collections.emptyList();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }
}
