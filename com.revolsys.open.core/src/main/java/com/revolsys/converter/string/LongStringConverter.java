package com.revolsys.converter.string;

import com.revolsys.util.Property;

public class LongStringConverter extends AbstractNumberStringConverter<Long> {
  public LongStringConverter() {
    super();
  }

  @Override
  public Class<Long> getConvertedClass() {
    return Long.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
  public Long toObject(final Object value) {
    if (value instanceof Long) {
      final Long integer = (Long)value;
      return integer;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.longValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Long toObject(final String string) {
    if (Property.hasValue(string)) {
      return Long.valueOf(string);
    } else {
      return null;
    }
  }
}
