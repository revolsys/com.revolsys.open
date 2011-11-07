package com.revolsys.collection;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

public class DelegatingIterator<T> extends AbstractIterator<T> {

  private LinkedList<T> objects;

  private Iterator<T> iterator;

  public DelegatingIterator(final Iterator<T> iterator) {
    this(iterator, Collections.<T> emptyList());
  }

  public DelegatingIterator(final Iterator<T> iterator,
    final Collection<? extends T> objects) {
    this.iterator = iterator;
    this.objects = new LinkedList<T>(objects);
  }

  @Override
  protected void doClose() {
    try {
      if (iterator instanceof AbstractIterator) {
        AbstractIterator<T> iter = (AbstractIterator<T>)iterator;
        iter.close();
      }
    } finally {
      objects = null;
      iterator = null;
    }
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    if (objects.isEmpty()) {
      final Iterator<T> i = iterator;
      return getNextIteratorObject(i);
    } else {
      return getNextSavedObject();
    }
  }

  protected T getNextIteratorObject(final Iterator<T> iterator) {
    if (iterator.hasNext()) {
      return iterator.next();
    } else {
      throw new NoSuchElementException();
    }
  }

  protected T getNextSavedObject() {
    return objects.removeFirst();
  }

  @Override
  public String toString() {
    return iterator.toString();
  }
}
