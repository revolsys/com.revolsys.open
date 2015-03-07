package com.revolsys.collection.list;

import java.util.List;

import com.revolsys.util.Property;

public class Lists {

  /**
   * Add the value to the list if it is not empty and not already in the list.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotContains(final List<V> list, final int index, final V value) {
    if (Property.hasValue(value)) {
      if (!list.contains(value)) {
        list.add(index, value);
        return true;
      }
    }
    return false;
  }

  /**
   * Add the value to the list if it is not empty and not already in the list.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotContains(final List<V> list, final V value) {
    if (Property.hasValue(value)) {
      if (!list.contains(value)) {
        return list.add(value);
      }
    }
    return false;
  }

  /**
   * Add the value to the list if it is not empty.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotEmpty(final List<V> list, final int index, final V value) {
    if (Property.hasValue(value)) {
      list.add(index, value);
      return true;
    } else {
      return false;
    }
  }

  /**
   * Add the value to the list if it is not empty.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotEmpty(final List<V> list, final V value) {
    if (Property.hasValue(value)) {
      return list.add(value);
    } else {
      return false;
    }
  }
}
