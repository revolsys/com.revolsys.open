package com.revolsys.record.query.functions;

import com.revolsys.record.Record;
import com.revolsys.record.query.QueryValue;
import com.revolsys.util.Property;

public class Lower extends UnaryFunction {

  public Lower(final QueryValue parameter) {
    super("LOWER", parameter);
  }

  @Override
  public String getStringValue(final Record record) {
    return getValue(record);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Record record) {
    final QueryValue parameter = getParameter();
    final String stringValue = parameter.getStringValue(record);
    if (Property.hasValue(stringValue)) {
      return (V)stringValue.toLowerCase();
    } else {
      return (V)stringValue;
    }
  }
}
