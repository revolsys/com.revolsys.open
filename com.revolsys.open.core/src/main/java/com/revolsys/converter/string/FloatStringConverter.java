package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class FloatStringConverter implements StringConverter<Float> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(Float value) {
    return value.toString();
  }

  public Float toObject(Object value) {
    if (value instanceof Float) {
      Float integer = (Float)value;
      return integer;
    } else if (value instanceof Number) {
      Number number = (Number)value;
      return number.floatValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Float toObject(String string) {
    if (StringUtils.hasText(string)) {
      return Float.valueOf(string);
    } else {
      return null;
    }
  }

  public Class<Float> getConvertedClass() {
    return Float.class;
  }
}
