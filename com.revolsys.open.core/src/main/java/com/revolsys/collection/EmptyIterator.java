package com.revolsys.collection;

import java.util.NoSuchElementException;

public class EmptyIterator<T> extends AbstractIterator<T> {
  public static <V> EmptyIterator<V> create() {
    return new EmptyIterator<>();
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    throw new NoSuchElementException();
  }
}
