package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.gis.data.model.Attribute;

public class Like extends BinaryCondition {

  public static Like like(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return like(name, valueCondition);
  }

  public static Like like(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return new Like(left, valueCondition);
  }

  public static Like like(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return like(name, valueCondition);
  }

  public static Like like(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new Like(leftCondition, right);
  }

  public Like(final QueryValue left, final QueryValue right) {
    super(left, "LIKE", right);
  }

  public Like(final String name, final Object value) {
    super(name, "LIKE", value);
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    // final QueryValue left = getLeft();
    // final Object value1 = left.getValue(record);
    //
    // final QueryValue right = getRight();
    // final Object value2 = right.getValue(record);
    //
    // return EqualsRegistry.equal(value1, value2);
    return true;
  }

  @Override
  public Like clone() {
    return (Like)super.clone();
  }

}
