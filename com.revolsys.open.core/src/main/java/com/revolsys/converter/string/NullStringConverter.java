package com.revolsys.converter.string;

public class NullStringConverter<T> implements StringConverter<T> {
  public boolean requiresQuotes() {
    return false;
  }

  public String toString(T number) {
    return "null";
  }

  public T toObject(Object value) {
    return null;
  }

  public T toObject(String string) {
    return null;
  }

  public Class<T> getConvertedClass() {
    return null;
  }

}
