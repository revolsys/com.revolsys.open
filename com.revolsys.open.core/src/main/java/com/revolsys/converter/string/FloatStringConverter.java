package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class FloatStringConverter implements StringConverter<Float> {
  public Class<Float> getConvertedClass() {
    return Float.class;
  }

  public boolean requiresQuotes() {
    return false;
  }

  public Float toObject(final Object value) {
    if (value instanceof Float) {
      final Float integer = (Float)value;
      return integer;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.floatValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Float toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Float.valueOf(string);
    } else {
      return null;
    }
  }

  public String toString(final Float value) {
    return value.toString();
  }
}
