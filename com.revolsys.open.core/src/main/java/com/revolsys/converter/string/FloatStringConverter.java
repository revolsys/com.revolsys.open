package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class FloatStringConverter extends AbstractNumberStringConverter<Float> {
  public FloatStringConverter() {
    super(DECIMAL_FORMAT);
  }

  @Override
  public Class<Float> getConvertedClass() {
    return Float.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
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

  @Override
  public Float toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Float.valueOf(string);
    } else {
      return null;
    }
  }
}
