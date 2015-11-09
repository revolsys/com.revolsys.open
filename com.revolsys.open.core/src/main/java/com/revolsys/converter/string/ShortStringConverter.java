package com.revolsys.converter.string;

import com.revolsys.util.Property;

public class ShortStringConverter extends AbstractNumberStringConverter<Short> {
  public ShortStringConverter() {
    super();
  }

  @Override
  public Class<Short> getConvertedClass() {
    return Short.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
  public Short objectToObject(final Object value) {
    if (value instanceof Short) {
      final Short integer = (Short)value;
      return integer;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.shortValue();
    } else if (value == null) {
      return null;
    } else {
      return stringToObject(value.toString());
    }
  }

  @Override
  public Short stringToObject(final String string) {
    if (Property.hasValue(string)) {
      return Short.valueOf(string);
    } else {
      return null;
    }
  }
}
