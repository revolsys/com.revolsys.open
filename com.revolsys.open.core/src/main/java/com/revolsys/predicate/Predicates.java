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

  public static <T> AndPredicate<T> and(final Iterable<Predicate<T>> predicates) {
    return new AndPredicate<>(predicates);
  }

  @SuppressWarnings("unchecked")
  public static <T> AndPredicate<T> and(final Predicate<T>... predicates) {
    return new AndPredicate<>(predicates);
  }

  public static <T> boolean matches(final List<T> objects, final Predicate<T> predicate) {
    for (final T object : objects) {
      if (predicate.test(object)) {
        return true;
      }
    }
    return false;
  }

  public static <T> boolean matches(final Predicate<T> predicate, final T object) {
    if (predicate == null) {
      return true;
    } else {
      if (predicate.test(object)) {
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
  public static <T> OrPredicate<T> or(final Predicate<T>... predicates) {
    return new OrPredicate<T>(predicates);
  }

  public static <T> List<T> predicate(final Collection<T> collection,
    final Predicate<T> predicate) {
    final List<T> list = new ArrayList<T>();
    predicateCopy(collection, list, predicate);
    return list;
  }

  public static <T> List<T> predicateAndRemove(final Collection<T> collection,
    final Predicate<T> predicate) {
    final List<T> list = new ArrayList<T>();
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T object = iterator.next();
      if (predicate.test(object)) {
        iterator.remove();
        list.add(object);
      }
    }
    return list;
  }

  public static <T> void predicateCopy(final Collection<T> source, final Collection<T> target,
    final Predicate<T> predicate) {
    for (final T value : source) {
      if (predicate.test(value)) {
        target.add(value);
      }
    }
  }

  public static <T> void remove(final Collection<T> collection, final Predicate<T> predicate) {
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T value = iterator.next();
      if (predicate.test(value)) {
        iterator.remove();
      }
    }
  }

  public static <T> void retain(final Collection<T> collection, final Predicate<T> predicate) {
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T value = iterator.next();
      if (!predicate.test(value)) {
        iterator.remove();
      }
    }
  }
}
