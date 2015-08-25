package com.revolsys.collection.list;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import com.revolsys.util.Property;

public interface Lists {
  public static <V> void addAll(final List<V> list, final Iterable<? extends V> values) {
    if (values != null) {
      for (final V value : values) {
        list.add(value);
      }
    }
  }

  public static <V> void addAll(final List<V> list,
    @SuppressWarnings("unchecked") final V... values) {
    if (values != null) {
      for (final V value : values) {
        list.add(value);
      }
    }
  }

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

  public static <V> List<V> array(final Iterable<? extends V> values) {
    final List<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }

  public static <V> ArrayList<V> array(@SuppressWarnings("unchecked") final V... values) {
    final ArrayList<V> list = new ArrayList<>();
    addAll(list, values);
    return list;
  }

  public static <V> Supplier<List<V>> arrayFactory() {
    return () -> {
      return new ArrayList<V>();
    };
  }

  public static <V> LinkedList<V> linked(@SuppressWarnings("unchecked") final V... values) {
    final LinkedList<V> list = new LinkedList<>();
    addAll(list, values);
    return list;
  }

  public static <V> Supplier<List<V>> linkedFactory() {
    return () -> {
      return new LinkedList<V>();
    };
  }

  public static <V> List<V> unmodifiable(final Iterable<? extends V> values) {
    return new UnmodifiableArrayList<V>(values);
  }

  public static <V> List<V> unmodifiable(@SuppressWarnings("unchecked") final V... values) {
    return new UnmodifiableArrayList<V>(values);
  }
}
