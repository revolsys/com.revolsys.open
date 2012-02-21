package com.revolsys.converter.string;

public interface StringConverter<T> {
  Class<T> getConvertedClass();

  boolean requiresQuotes();

  T toObject(Object value);

  T toObject(String string);

  String toString(Object value);
}
