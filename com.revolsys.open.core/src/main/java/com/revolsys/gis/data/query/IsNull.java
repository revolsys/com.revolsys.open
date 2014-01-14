package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.gis.data.model.Attribute;

public class IsNull extends RightUnaryCondition {

  public static IsNull column(final Attribute attribute) {
    final String name = attribute.getName();
    return column(name);
  }

  public static IsNull column(final String name) {
    final Column condition = new Column(name);
    return new IsNull(condition);
  }

  public IsNull(final QueryValue value) {
    super(value, "IS NULL");
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue queryValue = getValue();
    final Object value = queryValue.getValue(record);
    return value == null;
  }
}
