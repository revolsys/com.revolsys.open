package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.gis.data.model.Attribute;

public class ILike extends BinaryCondition {

  public static ILike iLike(final Attribute attribute, final Object value) {
    final String name = attribute.getName();
    final Value valueCondition = new Value(attribute, value);
    return iLike(name, valueCondition);
  }

  public static ILike iLike(final QueryValue left, final Object value) {
    final Value valueCondition = new Value(value);
    return new ILike(left, valueCondition);
  }

  public static ILike iLike(final String name, final Object value) {
    final Value valueCondition = new Value(value);
    return iLike(name, valueCondition);
  }

  public static ILike iLike(final String left, final QueryValue right) {
    final Column leftCondition = new Column(left);
    return new ILike(leftCondition, right);
  }

  public ILike(final QueryValue left, final QueryValue right) {
    super(left, "LIKE", right);
  }

  public ILike(final String name, final Object value) {
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
  public void appendSql(final StringBuffer buffer) {
    final QueryValue left = getLeft();
    final QueryValue right = getRight();

    buffer.append("UPPER(CAST(");
    if (left == null) {
      buffer.append("NULL");
    } else {
      left.appendSql(buffer);
    }
    buffer.append(" AS VARCHAR(4000))) LIKE UPPER(");
    if (right == null) {
      buffer.append("NULL");
    } else {
      right.appendSql(buffer);
    }
    buffer.append(")");
  }

  @Override
  public ILike clone() {
    return (ILike)super.clone();
  }

}
