package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class Equal extends BinaryCondition {

  public static Equal equal(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return equal(name, valueCondition);
  }

  public static Equal equal(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return new Equal(left, valueCondition);
  }

  public static Equal equal(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return equal(name, valueCondition);
  }

  public static Equal equal(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new Equal(leftCondition, right);
  }

  public Equal(final QueryValue left, final QueryValue right) {
    super(left, "=", right);
  }

  public Equal(final String name, final Object value) {
    super(name, "=", value);
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue left = getLeft();
    final Object value1 = left.getValue(record);

    final QueryValue right = getRight();
    final Object value2 = right.getValue(record);

    return EqualsRegistry.equal(value1, value2);
  }

  @Override
  public Equal clone() {
    return (Equal)super.clone();
  }

}
