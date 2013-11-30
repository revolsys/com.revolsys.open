package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.filter.Filter;

public abstract class Condition extends QueryValue implements
  Filter<Map<String, Object>> {
  @Override
  public boolean accept(final Map<String, Object> record) {
    throw new UnsupportedOperationException("Cannot filter using " + toString());
  }

  @Override
  public Condition clone() {
    return (Condition)super.clone();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Map<String, Object> record) {
    final Boolean value = accept(record);
    return (V)value;
  }

  public boolean isEmpty() {
    return false;
  }

  @Override
  public String toFormattedString() {
    return toString();
  }
}
