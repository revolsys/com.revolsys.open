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
  public T toObject(final Object value) {
    return null;
  }

  @Override
  public T toObject(final String string) {
    return null;
  }

  @Override
  public String toString(final Object value) {
    return "null";
  }

}
