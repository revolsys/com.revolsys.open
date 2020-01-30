package com.revolsys.record.io.format.json;

import java.util.Collection;
import java.util.List;

import org.jeometry.common.data.type.DataType;

public interface JsonList extends List<Object>, JsonType {
  static JsonList array() {
    return new JsonListArray();
  }

  static JsonList array(final Collection<?> collection) {
    return new JsonListArray(collection);
  }

  static JsonList array(final Object value) {
    return new JsonListArray(value);
  }

  static JsonList array(final Object... values) {
    return new JsonListArray(values);
  }

  default boolean addIfNotContains(final Object value) {
    final boolean contains = contains(value);
    if (!contains) {
      add(value);
    }
    return !contains;
  }

  default boolean equals(final Object value1, final Object value2,
    final Collection<? extends CharSequence> excludeFieldNames) {
    final List<?> list1 = (List<?>)value1;
    final List<?> list2 = (List<?>)value2;
    if (list1.size() != list2.size()) {
      return false;
    } else {
      for (int i = 0; i < list1.size(); i++) {
        final Object value11 = list1.get(i);
        final Object value21 = list2.get(i);
        if (!DataType.equal(value11, value21, excludeFieldNames)) {
          return false;
        }
      }
    }
    return true;
  }

  @SuppressWarnings("unchecked")
  default <V> V getValue(final int index) {
    return (V)get(index);
  }
}
