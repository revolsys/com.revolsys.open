package com.revolsys.gis.data.query;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.util.ExceptionUtil;

public abstract class QueryValue implements Cloneable {

  public static <V extends QueryValue> List<V> cloneQueryValues(
    final List<V> values) {
    final List<V> clonedValues = new ArrayList<V>();
    for (final V value : values) {
      @SuppressWarnings("unchecked")
      final V clonedValue = (V)value.clone();
      clonedValues.add(clonedValue);
    }
    return clonedValues;
  }

  // TODO wrap in a more generic structure
  public abstract int appendParameters(int index, PreparedStatement statement);

  public abstract void appendSql(StringBuffer buffer);

  @Override
  public QueryValue clone() {
    try {
      final QueryValue clone = (QueryValue)super.clone();
      return clone;
    } catch (final CloneNotSupportedException e) {
      ExceptionUtil.throwUncheckedException(e);
      return null;
    }
  }

  public List<QueryValue> getQueryValues() {
    return Collections.emptyList();
  }

  public abstract <V> V getValue(Map<String, Object> record);

  public String toFormattedString() {
    return toString();
  }

}
