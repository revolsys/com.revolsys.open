package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class ShortStringConverter implements StringConverter<Short> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(Short value) {
    return value.toString();
  }

  public Short toObject(Object value) {
    if (value instanceof Short) {
      Short integer = (Short)value;
      return integer;
    } else if (value instanceof Number) {
      Number number = (Number)value;
      return number.shortValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Short toObject(String string) {
    if (StringUtils.hasText(string)) {
      return Short.valueOf(string);
    } else {
      return null;
    }
  }

  public Class<Short> getConvertedClass() {
    return Short.class;
  }
}
