package com.revolsys.collection.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.util.Cancellable;

public class CancelIterable<T> extends AbstractIterator<T> {

  public static <V> Iterable<V> i(final Cancellable cancellable, final Iterable<V> iterable) {
    if (cancellable == null) {
      return iterable;
    } else {
      return new CancelIterable<>(cancellable, iterable);
    }
  }

  private final Cancellable cancellable;

  private Iterator<T> iterator;

  public CancelIterable(final Cancellable cancellable, final Iterable<T> iterable) {
    this.cancellable = cancellable;
    this.iterator = iterable.iterator();
  }

  @Override
  public void doClose() {
    try {
      if (this.iterator instanceof AutoCloseable) {
        final AutoCloseable closeable = (AutoCloseable)this.iterator;
        try {
          closeable.close();
        } catch (final Exception e) {
        }
      }
    } finally {
      this.iterator = null;
    }
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    if (this.cancellable.isCancelled()) {
      throw new NoSuchElementException();
    } else if (this.iterator.hasNext()) {
      return this.iterator.next();
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  public void remove() {
    this.iterator.remove();
  }

}
