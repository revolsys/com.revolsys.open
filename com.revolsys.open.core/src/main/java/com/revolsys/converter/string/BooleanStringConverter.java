package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class BooleanStringConverter implements StringConverter<Boolean> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(Boolean value) {
    return value.toString();
  }

  public Boolean toObject(Object value) {
    if (value instanceof Boolean) {
      Boolean integer = (Boolean)value;
      return integer;
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Boolean toObject(String string) {
    if (StringUtils.hasText(string)) {
      return Boolean.valueOf(string);
    } else {
      return null;
    }
  }

  public Class<Boolean> getConvertedClass() {
    return Boolean.class;
  }
}
