package com.revolsys.util;

import java.util.Iterator;
import java.util.function.Predicate;

import com.revolsys.collection.iterator.CancelIterable;
import com.revolsys.collection.iterator.Iterators;

public interface Cancellable {
  static Cancellable FALSE = () -> {
    return false;
  };

  default <V> Iterable<V> cancellable(final Iterable<V> iterable) {
    return new CancelIterable<>(this, iterable);
  }

  default <V> Iterable<V> cancellable(final Iterable<V> iterable, final Predicate<V> filter) {
    final Iterable<V> filteredIterator = Iterators.filter(iterable, filter);
    return new CancelIterable<>(this, filteredIterator);
  }

  default <V> Iterator<V> cancellable(final Iterator<V> iterator) {
    return new CancelIterable<>(this, iterator);
  }

  default <V> Iterator<V> cancellable(final Iterator<V> iterator, final Predicate<V> filter) {
    final Iterator<V> filteredIterator = Iterators.filter(iterator, filter);
    return new CancelIterable<>(this, filteredIterator);
  }

  boolean isCancelled();
}
