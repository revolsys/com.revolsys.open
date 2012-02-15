package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class LongStringConverter implements StringConverter<Long> {
  public Class<Long> getConvertedClass() {
    return Long.class;
  }

  public boolean requiresQuotes() {
    return false;
  }

  public Long toObject(final Object value) {
    if (value instanceof Long) {
      final Long integer = (Long)value;
      return integer;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Long toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Long.valueOf(string);
    } else {
      return null;
    }
  }

  public String toString(final Long value) {
    return value.toString();
  }
}
