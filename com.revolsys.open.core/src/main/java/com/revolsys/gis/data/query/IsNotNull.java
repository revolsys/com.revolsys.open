package com.revolsys.gis.data.query;

import java.util.Map;

import com.revolsys.gis.data.model.Attribute;

public class IsNotNull extends RightUnaryCondition {

  public static IsNotNull column(final Attribute attribute) {
    final String name = attribute.getName();
    return column(name);
  }

  public static IsNotNull column(final String name) {
    final Column condition = new Column(name);
    return new IsNotNull(condition);
  }

  public IsNotNull(final QueryValue value) {
    super(value, "IS NOT NULL");
  }

  @Override
  public boolean accept(final Map<String, Object> record) {
    final QueryValue queryValue = getValue();
    final Object value = queryValue.getValue(record);
    return value != null;
  }
}
