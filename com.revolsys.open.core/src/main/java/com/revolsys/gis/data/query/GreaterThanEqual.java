package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.util.CompareUtil;

public class GreaterThanEqual extends BinaryCondition {

  public static GreaterThanEqual greaterThanOrEqual(final Attribute attribute,
    final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return greaterThanOrEqual(name, valueCondition);
  }

  public static GreaterThanEqual greaterThanOrEqual(final String name,
    final Object value) {
    final Value valueCondition = new Value(value);
    return new GreaterThanEqual(name, valueCondition);
  }

  public static GreaterThanEqual greaterThanOrEqual(final String name,
    final QueryValue right) {
    final Column column = new Column(name);
    return new GreaterThanEqual(column, right);
  }

  public GreaterThanEqual(final QueryValue left, final QueryValue right) {
    super(left, ">=", right);
  }

  public GreaterThanEqual(final String name, final Object value) {
    super(name, ">=", value);
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return CompareUtil.compare(value1, value2) >= 0;
  }

  @Override
  public GreaterThanEqual clone() {
    return (GreaterThanEqual)super.clone();
  }

}
