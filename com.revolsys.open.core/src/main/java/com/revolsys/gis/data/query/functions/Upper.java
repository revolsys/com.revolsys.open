package com.revolsys.gis.data.query.functions;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.query.QueryValue;

public class Upper extends UnaryFunction {

  public Upper(final QueryValue parameter) {
    super("UPPER", parameter);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Map<String, Object> record) {

    final QueryValue parameter = getParameter();
    final String stringValue = parameter.getStringValue(record);
    if (StringUtils.hasText(stringValue)) {
      return (V)stringValue.toUpperCase();
    } else {
      return (V)stringValue;
    }
  }
}
