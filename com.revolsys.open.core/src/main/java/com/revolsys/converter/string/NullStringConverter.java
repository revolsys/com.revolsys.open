package com.revolsys.converter.string;

public class NullStringConverter<T> implements StringConverter<T> {
  public Class<T> getConvertedClass() {
    return null;
  }

  public boolean requiresQuotes() {
    return false;
  }

  public T toObject(final Object value) {
    return null;
  }

  public T toObject(final String string) {
    return null;
  }

  public String toString(final T number) {
    return "null";
  }

}
