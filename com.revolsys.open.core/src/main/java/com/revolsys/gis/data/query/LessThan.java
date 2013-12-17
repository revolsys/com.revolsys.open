package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.util.CompareUtil;

public class LessThan extends BinaryCondition {

  public static LessThan lessThan(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return lessThan(name, valueCondition);
  }

  public static LessThan lessThan(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return new LessThan(name, valueCondition);
  }

  public static LessThan lessThan(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return new LessThan(column, right);
  }

  public LessThan(final QueryValue left, final QueryValue right) {
    super(left, "<", right);
  }

  public LessThan(final String name, final Object value) {
    super(name, "<", value);
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return CompareUtil.compare(value1, value2) < 0;
  }

  @Override
  public LessThan clone() {
    return (LessThan)super.clone();
  }

}
