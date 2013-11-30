package com.revolsys.gis.data.query;

import java.util.Map;

public class IsNotNull extends RightUnaryCondition {

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
    Object value = queryValue.getValue(record);
    return value != null;
  }
}
