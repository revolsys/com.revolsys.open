package com.revolsys.converter.string;

import org.springframework.util.StringUtils;

public class ByteStringConverter implements StringConverter<Byte> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(Byte value) {
    return value.toString();
  }

  public Byte toObject(Object value) {
    if (value instanceof Byte) {
      Byte integer = (Byte)value;
      return integer;
    } else if (value instanceof Number) {
      Number number = (Number)value;
      return number.byteValue();
    } else if (value == null) {
      return null;
    } else {
      return toObject(value.toString());
    }
  }

  public Byte toObject(String string) {
    if (StringUtils.hasText(string)) {
      return Byte.valueOf(string);
    } else {
      return null;
    }
  }

  public Class<Byte> getConvertedClass() {
    return Byte.class;
  }
}
