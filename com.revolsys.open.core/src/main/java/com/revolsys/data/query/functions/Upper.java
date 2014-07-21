package com.revolsys.data.query.functions;

import java.util.Map;

import com.revolsys.data.query.QueryValue;
import com.revolsys.util.Property;

public class Upper extends UnaryFunction {

  public Upper(final QueryValue parameter) {
    super("UPPER", parameter);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Map<String, Object> record) {

    final QueryValue parameter = getParameter();
    final String stringValue = parameter.getStringValue(record);
    if (Property.hasValue(stringValue)) {
      return (V)stringValue.toUpperCase();
    } else {
      return (V)stringValue;
    }
  }
}
