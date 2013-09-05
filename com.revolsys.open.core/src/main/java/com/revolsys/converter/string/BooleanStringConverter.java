package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class BooleanStringConverter implements StringConverter<Boolean> {
  public static boolean getBoolean(final Object value) {
    final Boolean bool = valueOf(value);
    return bool != null && bool;
  }

  public static boolean isFalse(final Object value) {
    final Boolean bool = valueOf(value);
    if (bool == null) {
      return false;
    } else {
      return !bool;
    }
  }

  public static boolean isTrue(final Object value) {
    final Boolean bool = valueOf(value);
    if (bool == null) {
      return false;
    } else {
      return bool;
    }
  }

  public static Boolean valueOf(final Object value) {
    if (value instanceof Boolean) {
      final Boolean bool = (Boolean)value;
      return bool;
    } else if (value == null) {
      return null;
    } else {
      return valueOf(value.toString());
    }
  }

  public static Boolean valueOf(final String string) {
    if (StringUtils.hasText(string)) {
      return Boolean.valueOf(string);
    } else {
      return null;
    }
  }

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
    return valueOf(value);
  }

  @Override
  public Boolean toObject(final String string) {
    return valueOf(string);
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
