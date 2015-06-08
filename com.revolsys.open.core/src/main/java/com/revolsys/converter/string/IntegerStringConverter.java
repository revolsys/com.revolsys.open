package com.revolsys.converter.string;

import com.revolsys.util.Property;

public class IntegerStringConverter extends AbstractNumberStringConverter<Integer> {
  public IntegerStringConverter() {
    super();
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
    if (Property.hasValue(string)) {
      return Integer.valueOf(string);
    } else {
      return null;
    }
  }

}
