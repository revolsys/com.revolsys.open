package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class BooleanStringConverter implements StringConverter<Boolean> {
  public Class<Boolean> getConvertedClass() {
    return Boolean.class;
  }

  public boolean requiresQuotes() {
    return false;
  }

  public Boolean toObject(final Object value) {
    if (value instanceof Boolean) {
      final Boolean integer = (Boolean)value;
      return integer;
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Boolean toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Boolean.valueOf(string);
    } else {
      return null;
    }
  }

  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Boolean) {
      return value.toString();
    } else {
      if ("true".equalsIgnoreCase(value.toString())) {
        return "true";
      } else {
        return "false";
      }
    }
  }
}
