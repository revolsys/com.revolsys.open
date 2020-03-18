package com.revolsys.record.query.functions;

import com.revolsys.record.Record;
import com.revolsys.record.query.QueryValue;
import com.revolsys.record.query.Value;
import com.revolsys.util.Property;

public class RegexpReplace extends Function {

  public RegexpReplace(final QueryValue value, final String pattern, final String replace) {
    super("regexp_replace", value, new Value(pattern), new Value(replace));
  }

  public RegexpReplace(final QueryValue value, final String pattern, final String replace,
    final String flags) {
    super("regexp_replace", value, new Value(pattern), new Value(replace), new Value(flags));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Record record) {

    final String text = getParameterStringValue(0, record);
    final String pattern = getParameterStringValue(1, record);
    String replace = getParameterStringValue(2, record);
    if (replace == null) {
      replace = "";
    }
    if (Property.hasValue(text)) {
      return (V)text.replaceAll(pattern, replace);
    } else {
      return null;
    }
  }
}
