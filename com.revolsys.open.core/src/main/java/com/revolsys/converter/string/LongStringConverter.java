package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class LongStringConverter implements StringConverter<Long> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(Long value) {
    return value.toString();
  }

  public Long toObject(Object value) {
    if (value instanceof Long) {
      Long integer = (Long)value;
      return integer;
    } else if (value instanceof Number) {
      Number number = (Number)value;
      return number.longValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Long toObject(String string) {
    if (StringUtils.hasText(string)) {
      return Long.valueOf(string);
    } else {
      return null;
    }
  }
}
