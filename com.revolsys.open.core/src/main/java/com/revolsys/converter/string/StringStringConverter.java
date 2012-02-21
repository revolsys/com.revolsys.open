package com.revolsys.converter.string;

public class StringStringConverter implements StringConverter<String> {
  public Class<String> getConvertedClass() {
    return String.class;
  }

  public boolean requiresQuotes() {
    return true;
  }

  public String toObject(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

  public String toObject(final String string) {
    return string;
  }

  public String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

}
