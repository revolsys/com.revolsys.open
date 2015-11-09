package com.revolsys.converter.string;

public class NullStringConverter<T> implements StringConverter<T> {
  @Override
  public Class<T> getConvertedClass() {
    return null;
  }

  @Override
  public boolean requiresQuotes() {
    return false;
  }

  @Override
  public T objectToObject(final Object value) {
    return null;
  }

  @Override
  public T stringToObject(final String string) {
    return null;
  }

  @Override
  public String objectToString(final Object value) {
    return "null";
  }

}
