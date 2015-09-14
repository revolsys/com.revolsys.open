package com.revolsys.record.query.functions;

import java.util.Map;

import com.revolsys.record.query.QueryValue;
import com.revolsys.util.Property;

public class Lower extends UnaryFunction {

  public Lower(final QueryValue parameter) {
    super("LOWER", parameter);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Map<String, Object> record) {

    final QueryValue parameter = getParameter();
    final String stringValue = parameter.getStringValue(record);
    if (Property.hasValue(stringValue)) {
      return (V)stringValue.toLowerCase();
    } else {
      return (V)stringValue;
    }
  }
}
