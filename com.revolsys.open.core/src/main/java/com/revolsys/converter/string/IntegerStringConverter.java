package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class IntegerStringConverter implements StringConverter<Integer> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(Integer value) {
    return value.toString();
  }

  public Integer toObject(Object value) {
    if (value instanceof Integer) {
      Integer integer = (Integer)value;
      return integer;
    } else if (value instanceof Number) {
      Number number = (Number)value;
      return number.intValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Integer toObject(String string) {
    if (StringUtils.hasText(string)) {
      return Integer.valueOf(string);
    } else {
      return null;
    }
  }
}
