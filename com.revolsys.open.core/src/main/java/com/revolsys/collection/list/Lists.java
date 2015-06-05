package com.revolsys.collection.list;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.util.Property;

public class Lists {

  static <V> void addAll(final List<V> list, final Iterable<V> values) {
    for (final V value : values) {
      list.add(value);
    }
  }

  static <V> void addAll(final List<V> list,
    @SuppressWarnings("unchecked") final V... values) {
    for (final V value : values) {
      list.add(value);
    }
  }

  /**
   * Add the value to the list if it is not empty and not already in the list.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotContains(final List<V> list, final int index,
    final V value) {
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
   * Add the value to the list if it is not empty and not already in the list.
   * @param list
   * @param value
   * @return
   */
  public static <V> boolean addNotContainsLast(final List<V> list, final V value) {
    if (Property.hasValue(value)) {
      if (list.isEmpty() || !list.get(list.size() - 1).equals(value)) {
        list.add(value);
        return true;
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
  public static <V> boolean addNotEmpty(final List<V> list, final int index,
    final V value) {
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

  public static <V> List<V> array(final Iterable<V> values) {
    final List<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }

  public static <V> List<V> array(
    @SuppressWarnings("unchecked") final V... values) {
    final List<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }
}
