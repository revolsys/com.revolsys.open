package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class IntegerStringConverter extends
  AbstractNumberStringConverter<Integer> {
  public IntegerStringConverter() {
    super(INTEGER_FORMAT);
  }

  @Override
  public Class<Integer> getConvertedClass() {
    return Integer.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
  public Integer toObject(final Object value) {
    if (value instanceof Integer) {
      final Integer integer = (Integer)value;
      return integer;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.intValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Integer toObject(final String string) {
    if (StringUtils.hasText(string)) {
      return Integer.valueOf(string);
    } else {
      return null;
    }
  }

}
