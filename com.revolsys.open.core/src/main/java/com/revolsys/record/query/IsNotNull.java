package com.revolsys.record.query;

import com.revolsys.record.Record;

public class IsNotNull extends RightUnaryCondition {

  public IsNotNull(final QueryValue value) {
    super(value, "IS NOT NULL");
  }

  @Override
  public boolean test(final Record record) {
    final QueryValue queryValue = getValue();
    final Object value = queryValue.getValue(record);
    return value != null;
  }
}
