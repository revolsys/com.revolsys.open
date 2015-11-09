package com.revolsys.converter.string;

import com.revolsys.datatype.DataType;

public interface StringConverter<T> {
  public static <C> StringConverter<C> getConverter(final Class<C> clazz) {
    return StringConverterRegistry.getInstance().getConverter(clazz);
  }

  public static <C> StringConverter<C> getConverter(final Object value) {
    return StringConverterRegistry.getInstance().getConverter(value);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  static <V> V toObject(final Class valueClass, final Object value) {
    if (value == null) {
      return null;
    } else {
      final StringConverter<Object> converter = getConverter(valueClass);
      if (converter == null) {
        return (V)value;
      } else {
        return (V)converter.objectToObject(value);
      }
    }
  }

  @SuppressWarnings({
    "unchecked"
  })
  static <V> V toObject(final DataType dataType, final Object value) {
    final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
    return (V)toObject(dataTypeClass, value);
  }

  static Object toObject(final Object value) {
    if (value == null) {
      return null;
    } else {
      @SuppressWarnings("unchecked")
      final Class<Object> valueClass = (Class<Object>)value.getClass();
      return StringConverter.toObject(valueClass, value);
    }
  }

  @SuppressWarnings({
    "rawtypes", "unchecked"
  })
  static String toString(final Class valueClass, final Object value) {
    if (value == null) {
      return null;
    } else {
      final StringConverterRegistry registry = StringConverterRegistry.getInstance();
      final StringConverter<Object> converter = registry.getConverter(valueClass);
      if (converter == null) {
        return value.toString();
      } else {
        return converter.objectToString(value);
      }
    }
  }

  static String toString(final DataType dataType, final Object value) {
    @SuppressWarnings("unchecked")
    final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
    return toString(dataTypeClass, value);
  }

  @SuppressWarnings("unchecked")
  static String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Class<Object> valueClass = (Class<Object>)value.getClass();
      return StringConverter.toString(valueClass, value);
    }
  }

  Class<T> getConvertedClass();

  T objectToObject(Object value);

  String objectToString(Object value);

  boolean requiresQuotes();

  T stringToObject(String string);
}
