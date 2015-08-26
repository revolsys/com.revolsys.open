package com.revolsys.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class Predicates {
  public static <T> Predicate<T> all() {
    return (t) -> {
      return true;
    };
  }

  public static <T> AndPredicate<T> and(final Iterable<Predicate<T>> filters) {
    return new AndPredicate<>(filters);
  }

  @SuppressWarnings("unchecked")
  public static <T> AndPredicate<T> and(final Predicate<T>... filters) {
    return new AndPredicate<>(filters);
  }

  public static <T> List<T> filter(final Collection<T> collection, final Predicate<T> filter) {
    final List<T> list = new ArrayList<T>();
    filterCopy(collection, list, filter);
    return list;
  }

  public static <T> List<T> filterAndRemove(final Collection<T> collection,
    final Predicate<T> filter) {
    final List<T> list = new ArrayList<T>();
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T object = iterator.next();
      if (filter.test(object)) {
        iterator.remove();
        list.add(object);
      }
    }
    return list;
  }

  public static <T> void filterCopy(final Collection<T> source, final Collection<T> target,
    final Predicate<T> filter) {
    for (final T value : source) {
      if (filter.test(value)) {
        target.add(value);
      }
    }
  }

  public static <T> boolean matches(final List<T> objects, final Predicate<T> filter) {
    for (final T object : objects) {
      if (filter.test(object)) {
        return true;
      }
    }
    return false;
  }

  public static <T> boolean matches(final Predicate<T> filter, final T object) {
    if (filter == null) {
      return true;
    } else {
      if (filter.test(object)) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static <T> Predicate<T> none() {
    return (t) -> {
      return false;
    };
  }

  @SuppressWarnings("unchecked")
  public static <T> OrPredicate<T> or(final Predicate<T>... filters) {
    return new OrPredicate<T>(filters);
  }

  public static <T> void remove(final Collection<T> collection, final Predicate<T> filter) {
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T value = iterator.next();
      if (filter.test(value)) {
        iterator.remove();
      }
    }
  }

  public static <T> void retain(final Collection<T> collection, final Predicate<T> filter) {
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T value = iterator.next();
      if (!filter.test(value)) {
        iterator.remove();
      }
    }
  }
}
