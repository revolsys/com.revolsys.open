package com.revolsys.collection;

import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.revolsys.io.FileUtil;

public abstract class AbstractMultipleIterator<T> extends AbstractIterator<T> {
  private AbstractIterator<T> iterator;

  @Override
  @PreDestroy
  public void doClose() {
    if (this.iterator != null) {
      FileUtil.closeSilent(this.iterator);
      this.iterator = null;
    }
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    try {
      if (this.iterator == null) {
        this.iterator = getNextIterator();
      }
      while (!this.iterator.hasNext()) {
        this.iterator.close();
        this.iterator = getNextIterator();
      }
      return this.iterator.next();
    } catch (final NoSuchElementException e) {
      this.iterator = null;
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
