package com.revolsys.converter.string;

import com.revolsys.util.Property;

public class FloatStringConverter extends AbstractNumberStringConverter<Float> {
  public FloatStringConverter() {
    super();
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
  public Float objectToObject(final Object value) {
    if (value instanceof Float) {
      final Float integer = (Float)value;
      return integer;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.floatValue();
    } else if (value == null) {
      return null;
    } else {
      return stringToObject(value.toString());
    }
  }

  @Override
  public Float stringToObject(final String string) {
    if (Property.hasValue(string)) {
      return Float.valueOf(string);
    } else {
      return null;
    }
  }
}
