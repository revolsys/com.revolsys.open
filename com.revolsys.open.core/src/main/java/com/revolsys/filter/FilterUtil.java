package com.revolsys.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class FilterUtil {
  public static <T> List<T> filter(final Collection<T> collection,
    final Filter<T> filter) {
    final List<T> list = new ArrayList<T>();
    filterCopy(collection, list, filter);
    return list;
  }

  public static <T> List<T> filterAndRemove(final Collection<T> collection,
    final Filter<T> filter) {
    final List<T> list = new ArrayList<T>();
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T object = iterator.next();
      if (filter.accept(object)) {
        iterator.remove();
        list.add(object);
      }
    }
    return list;
  }

  public static <T> void filterCopy(final Collection<T> source,
    final Collection<T> target, final Filter<T> filter) {
    for (final T value : source) {
      if (filter.accept(value)) {
        target.add(value);
      }
    }
  }

  public static <T> boolean matches(final Filter<T> filter, final T object) {
    if (filter == null) {
      return true;
    } else {
      if (filter.accept(object)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static <T> boolean matches(final List<T> objects,
    final Filter<T> filter) {
    for (final T object : objects) {
      if (filter.accept(object)) {
        return true;
      }
    }
    return false;
  }

  public static <T> void remove(final Collection<T> collection,
    final Filter<T> filter) {
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T value = iterator.next();
      if (filter.accept(value)) {
        iterator.remove();
      }
    }
  }

  public static <T> void retain(final Collection<T> collection,
    final Filter<T> filter) {
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T value = iterator.next();
      if (!filter.accept(value)) {
        iterator.remove();
      }
    }
  }
}
