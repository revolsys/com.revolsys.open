package com.revolsys.converter.string;

public interface StringConverter<T> {
  boolean requiresQuotes();

  String toString(T value);

  T toObject(String string);
  
  T toObject(Object value);
}
