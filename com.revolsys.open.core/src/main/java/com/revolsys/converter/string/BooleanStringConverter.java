package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class BooleanStringConverter implements StringConverter<Boolean> {
  @Override
  public Class<Boolean> getConvertedClass() {
    return Boolean.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
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

  @Override
  public Boolean toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Boolean.valueOf(string);
    } else {
      return null;
    }
  }

  @Override
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
