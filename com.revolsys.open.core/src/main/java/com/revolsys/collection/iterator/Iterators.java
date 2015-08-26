package com.revolsys.collection.iterator;

import java.util.Iterator;
import java.util.function.Predicate;

public class Iterators {
  public static <V> Iterable<V> filter(final Iterable<V> iterable, final Predicate<V> filter) {
    if (filter == null) {
      return iterable;
    } else {
      final Iterator<V> iterator = iterable.iterator();
      return new FilterIterator<>(filter, iterator);
    }
  }

  public static <V> Iterator<V> filter(final Iterator<V> iterator, final Predicate<V> filter) {
    if (filter == null) {
      return iterator;
    } else {
      return new FilterIterator<>(filter, iterator);
    }
  }
}
