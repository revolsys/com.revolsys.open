package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.util.CompareUtil;

public class GreaterThan extends BinaryCondition {

  public static GreaterThan greaterThan(final String name,
    final QueryValue right) {
    final Column column = new Column(name);
    return new GreaterThan(column, right);
  }

  public GreaterThan(final QueryValue left, final QueryValue right) {
    super(left, ">", right);
  }

  public GreaterThan(final String name, final Object value) {
    super(name, ">", value);
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return CompareUtil.compare(value1, value2) > 0;
  }

  @Override
  public GreaterThan clone() {
    return (GreaterThan)super.clone();
  }

}
