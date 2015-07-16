package com.revolsys.data.query;

import java.util.Map;

public class IsNotNull extends RightUnaryCondition {

  public IsNotNull(final QueryValue value) {
    super(value, "IS NOT NULL");
  }

  @Override
  public boolean test(final Map<String, Object> record) {
    final QueryValue queryValue = getValue();
    final Object value = queryValue.getValue(record);
    return value != null;
  }
}
