package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class DoubleStringConverter implements StringConverter<Double> {
  public Class<Double> getConvertedClass() {
    return Double.class;
  }

  public boolean requiresQuotes() {
    return false;
  }

  public Double toObject(final Object value) {
    if (value instanceof Double) {
      final Double integer = (Double)value;
      return integer;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Double toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Double.valueOf(string);
    } else {
      return null;
    }
  }

  public String toString(final Double value) {
    return value.toString();
  }
}
