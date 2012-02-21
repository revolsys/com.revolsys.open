package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class DoubleStringConverter extends
  AbstractNumberStringConverter<Double> {
  public DoubleStringConverter() {
    super(DECIMAL_FORMAT);
  }
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
}
