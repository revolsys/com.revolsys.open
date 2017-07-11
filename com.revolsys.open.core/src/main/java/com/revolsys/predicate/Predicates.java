package com.revolsys.predicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.util.Property;

public interface Predicates {
  static <T> boolean add(final Collection<T> collection, final T value,
    final Predicate<? super T> filter) {
    if (filter == null) {
      return collection.add(value);
    } else {
      if (filter.test(value)) {
        return collection.add(value);
      } else {
        return false;
      }
    }
  }

  static <T> void addAll(final Collection<T> collection, final Iterable<T> values,
    final Predicate<? super T> filter) {
    if (filter == null) {
      CollectionUtil.addAll(collection, values);
    } else {
      for (final T value : values) {
        if (filter.test(value)) {
          collection.add(value);
        }
      }
    }
  }

  static <T> Predicate<T> all() {
    return (t) -> {
      return true;
    };
  }

  static <T> AndPredicate<T> and(final Iterable<Predicate<T>> filters) {
    return new AndPredicate<>(filters);
  }

  @SuppressWarnings("unchecked")
  static <T> AndPredicate<T> and(final Predicate<T>... filters) {
    return new AndPredicate<>(filters);
  }

  static <V> int count(final Collection<V> values, final Predicate<? super V> filter) {
    if (Property.isEmpty(values)) {
      return 0;
    } else if (Property.isEmpty(filter)) {
      return values.size();
    } else {
      int count = 0;
      for (final V value : values) {
        if (filter.test(value)) {
          count++;
        }
      }
      return count;
    }
  }

  static <V> int count(final Iterable<V> values, final Predicate<? super V> filter) {
    int count = 0;
    for (final V value : values) {
      if (filter.test(value)) {
        count++;
      }
    }
    return count;
  }

  static <T> List<T> filter(final Iterable<T> collection, final Predicate<? super T> filter) {
    final List<T> list = new ArrayList<>();
    addAll(list, collection, filter);
    return list;
  }

  static <T> List<T> filterAndRemove(final Collection<T> collection,
    final Predicate<? super T> filter) {
    final List<T> list = new ArrayList<>();
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

  static <T> boolean matches(final List<T> objects, final Predicate<? super T> filter) {
    for (final T object : objects) {
      if (filter.test(object)) {
        return true;
      }
    }
    return false;
  }

  static <T> boolean matches(final Predicate<? super T> filter, final T object) {
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

  static <T> Consumer<? super T> newConsumer(final Predicate<? super T> filter,
    final Consumer<? super T> consumer) {
    if (filter == null) {
      return consumer;
    } else {
      return (value) -> {
        if (filter.test(value)) {
          consumer.accept(value);
        }
      };
    }
  }

  static <T> Predicate<T> none() {
    return (t) -> {
      return false;
    };
  }

  @SuppressWarnings("unchecked")
  static <T> OrPredicate<T> or(final Predicate<T>... filters) {
    return new OrPredicate<>(filters);
  }

  static <T> void remove(final Collection<T> collection, final Predicate<? super T> filter) {
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T value = iterator.next();
      if (filter.test(value)) {
        iterator.remove();
      }
    }
  }

  static <T> void retain(final Collection<T> collection, final Predicate<? super T> filter) {
    final Iterator<T> iterator = collection.iterator();
    while (iterator.hasNext()) {
      final T value = iterator.next();
      if (!filter.test(value)) {
        iterator.remove();
      }
    }
  }

  static <V> boolean test(final Predicate<? super V> filter, final V value) {
    if (filter == null) {
      return true;
    } else {
      return filter.test(value);
    }
  }
}
