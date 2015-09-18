package com.revolsys.datatype;

import com.revolsys.converter.string.StringConverter;

public interface DataType {

  <V> StringConverter<V> getConverter();

  Class<?> getJavaClass();

  String getName();

  String getValidationName();

  default <V> V toObject(final Object value) {
    final StringConverter<V> converter = getConverter();
    return converter.toObject(value);
  }

  default String toString(final Object value) {
    final StringConverter<?> converter = getConverter();
    return converter.toString(value);
  }
}
