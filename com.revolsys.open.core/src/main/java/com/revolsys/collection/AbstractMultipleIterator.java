package com.revolsys.collection;

import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

public abstract class AbstractMultipleIterator<T> extends AbstractIterator<T> {
  private AbstractIterator<T> iterator;

  @PreDestroy
  public final void doClose() {
    if (iterator != null) {
      iterator.close();
      iterator = null;
    }
  }

  protected T getNext() throws NoSuchElementException {
    try {
      if (iterator == null) {
        iterator = getNextIterator();
      }
      while (!iterator.hasNext()) {
        iterator.close();
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
  public abstract AbstractIterator<T> getNextIterator()
    throws NoSuchElementException;

}
