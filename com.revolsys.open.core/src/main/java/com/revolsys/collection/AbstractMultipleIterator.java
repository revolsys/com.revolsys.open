package com.revolsys.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

public abstract class AbstractMultipleIterator<T> extends AbstractIterator<T> {
  private Iterator<T> iterator;

  @PreDestroy
  public final void doClose() {
    iterator = null;
  }

  protected T getNext() throws NoSuchElementException {
    try {
      if (iterator == null) {
        iterator = getNextIterator();
      }
      while (!iterator.hasNext()) {
        iterator = getNextIterator();
      }
      return iterator.next();
    } catch (NoSuchElementException e) {
      iterator = null;
      throw e;
    }
  }

  /**
   * Get the next iterator, if no iterators are available throw
   * {@link NoSuchElementException}. Don't not return null.
   * 
   * @return
   * @throws NoSuchElementException
   */
  public abstract Iterator<T> getNextIterator() throws NoSuchElementException;

}
