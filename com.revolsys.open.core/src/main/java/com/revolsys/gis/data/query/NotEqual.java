package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class NotEqual extends BinaryCondition {

  public static NotEqual notEqual(final String name, final Object value) {
    return notEqual(name, new Value(value));
  }

  public static NotEqual notEqual(final String name, final QueryValue right) {
    final Column column = new Column(name);
    return new NotEqual(column, right);
  }

  public NotEqual(final QueryValue left, final QueryValue right) {
    super(left, "<>", right);
  }

  public NotEqual(final String name, final Object value) {
    super(name, "<>", value);
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return !EqualsRegistry.equal(value1, value2);
  }

  @Override
  public NotEqual clone() {
    return (NotEqual)super.clone();
  }

}
