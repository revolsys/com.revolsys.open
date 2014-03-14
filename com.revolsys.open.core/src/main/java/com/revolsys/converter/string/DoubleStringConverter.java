package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class DoubleStringConverter extends
  AbstractNumberStringConverter<Double> {
  public DoubleStringConverter() {
    super();
  }

  @Override
  public Class<Double> getConvertedClass() {
    return Double.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
  public Double toObject(final Object value) {
    if (value instanceof Double) {
      final Double number = (Double)value;
      return number;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.doubleValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Double toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Double.valueOf(string);
    } else {
      return null;
    }
  }
}
