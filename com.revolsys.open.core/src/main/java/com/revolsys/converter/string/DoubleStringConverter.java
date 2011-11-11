package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class DoubleStringConverter implements StringConverter<Double> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(Double value) {
    return value.toString();
  }

  public Double toObject(Object value) {
    if (value instanceof Double) {
      Double integer = (Double)value;
      return integer;
    } else if (value instanceof Number) {
      Number number = (Number)value;
      return number.doubleValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Double toObject(String string) {
    if (StringUtils.hasText(string)) {
      return Double.valueOf(string);
    } else {
      return null;
    }
  }
}
