package com.revolsys.datatype;

public interface DataType {
  Class<?> getJavaClass();

  String getName();

  default String getValidationName() {
    if (Number.class.isAssignableFrom(getJavaClass())) {
      return "number (" + getName() + ")";
    } else {
      return getName();
    }
  }

  default boolean isRequiresQuotes() {
    return true;
  }

  @SuppressWarnings("unchecked")
  default <V> V toObject(final Object value) {
    return (V)value;
  }

  @SuppressWarnings("unchecked")
  default <V> V toObject(final String value) {
    return (V)toObject((Object)value);
  }

  default String toString(final Object value) {
    if (value == null) {
      return null;
    } else {
      return value.toString();
    }
  }
}
