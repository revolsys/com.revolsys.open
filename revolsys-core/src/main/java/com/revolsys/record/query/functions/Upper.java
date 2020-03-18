package com.revolsys.record.query.functions;

import com.revolsys.record.Record;
import com.revolsys.record.query.QueryValue;
import com.revolsys.util.Property;

public class Upper extends UnaryFunction {

  public Upper(final QueryValue parameter) {
    super("UPPER", parameter);
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
      return (V)stringValue.toUpperCase();
    } else {
      return (V)stringValue;
    }
  }
}
