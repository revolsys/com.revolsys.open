package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class ShortStringConverter extends AbstractNumberStringConverter<Short> {
  public ShortStringConverter() {
    super(INTEGER_FORMAT);
  }

  public Class<Short> getConvertedClass() {
    return Short.class;
  }

  public boolean requiresQuotes() {
    return false;
  }

  public Short toObject(final Object value) {
    if (value instanceof Short) {
      final Short integer = (Short)value;
      return integer;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.shortValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Short toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Short.valueOf(string);
    } else {
      return null;
    }
  }
}
