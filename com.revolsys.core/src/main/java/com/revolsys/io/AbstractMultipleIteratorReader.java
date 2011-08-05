package com.revolsys.io;

import java.util.Iterator;

import com.revolsys.collection.AbstractIterator;

public abstract class AbstractMultipleIteratorReader<T> extends
  AbstractReader<T> implements Iterator<T> {

  private AbstractIterator<T> iterator;

  public void close() {
    iterator = null;
    iterator.close();
  }

  public void open() {
  }

  public Iterator<T> iterator() {
    open();
    return this;
  }

  public boolean hasNext() {
    if (iterator == null) {
      iterator = getNextIterator();
      if (iterator == null) {
        return false;
      }
    }
    while (!iterator.hasNext()) {
      iterator.close();
      iterator = getNextIterator();
      if (iterator == null) {
        return false;
      }
    }
    return true;
  }

  protected abstract AbstractIterator<T> getNextIterator();

  public T next() {
    return iterator.next();
  }

  public void remove() {
    iterator.remove();
  }
}
