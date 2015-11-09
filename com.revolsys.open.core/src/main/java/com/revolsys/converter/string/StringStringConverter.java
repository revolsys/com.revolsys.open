package com.revolsys.converter.string;

public class StringStringConverter implements StringConverter<String> {
  @Override
  public Class<String> getConvertedClass() {
    return String.class;
  }

  @Override
  public boolean requiresQuotes() {
    return true;
  }

  @Override
  public String objectToObject(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

  @Override
  public String stringToObject(final String string) {
    return string;
  }

  @Override
  public String objectToString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }

}
