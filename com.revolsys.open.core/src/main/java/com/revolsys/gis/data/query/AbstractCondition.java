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
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public String toString() {
    final StringBuffer string = new StringBuffer();
    appendSql(string);
    return string.toString();
  }
}
