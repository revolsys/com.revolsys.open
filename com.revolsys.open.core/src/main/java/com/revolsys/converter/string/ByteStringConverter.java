package com.revolsys.converter.string;

import com.revolsys.util.Property;

public class ByteStringConverter extends AbstractNumberStringConverter<Byte> {
  public ByteStringConverter() {
    super();
  }

  @Override
  public Class<Byte> getConvertedClass() {
    return Byte.class;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
  public Byte toObject(final Object value) {
    if (value instanceof Byte) {
      final Byte integer = (Byte)value;
      return integer;
    } else if (value instanceof Number) {
      final Number number = (Number)value;
      return number.byteValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  @Override
  public Byte toObject(final String string) {
    if (Property.hasValue(string)) {
      return Byte.valueOf(string);
    } else {
      return null;
    }
  }
}
