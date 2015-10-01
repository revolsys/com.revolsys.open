package com.revolsys.util;

import java.util.Iterator;

import com.revolsys.collection.iterator.CancelIterable;

public interface Cancellable {
  default <V> Iterable<V> cancellable(final Iterable<V> iterable) {
    return new CancelIterable<>(this, iterable);
  }

  default <V> Iterator<V> cancellable(final Iterator<V> iterator) {
    return new CancelIterable<>(this, iterator);
  }

  boolean isCancelled();
}
