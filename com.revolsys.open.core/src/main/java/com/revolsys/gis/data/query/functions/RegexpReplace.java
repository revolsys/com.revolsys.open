package com.revolsys.gis.data.query.functions;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.gis.data.query.QueryValue;
import com.revolsys.gis.data.query.Value;

public class RegexpReplace extends Function {

  public RegexpReplace(final QueryValue value, final String pattern,
    final String replace) {
    super("regexp_replace", value, new Value(pattern), new Value(replace));
  }

  public RegexpReplace(final QueryValue value, final String pattern,
    final String replace, final String flags) {
    super("regexp_replace", value, new Value(pattern), new Value(replace),
      new Value(flags));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Map<String, Object> record) {

    final String text = getParameterStringValue(0, record);
    final String pattern = getParameterStringValue(1, record);
    final String replace = getParameterStringValue(2, record);
    if (StringUtils.hasText(text)) {
      return (V)text.replaceAll(pattern, replace);
    } else {
      return null;
    }
  }
}
