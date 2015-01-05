package com.revolsys.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import javax.annotation.PreDestroy;

import com.revolsys.io.AbstractObjectWithProperties;

public abstract class AbstractIterator<T> extends AbstractObjectWithProperties
implements Iterator<T>, Iterable<T>, AutoCloseable {

  private boolean hasNext = true;

  private boolean initialized;

  private boolean loadNext = true;

  private T object;

  @Override
  @PreDestroy
  public final void close() {
    this.hasNext = false;
    this.object = null;
    doClose();
  }

  protected void doClose() {
  }

  protected void doInit() {
  }

  @Override
  protected void finalize() throws Throwable {
    close();
  }

  protected abstract T getNext() throws NoSuchElementException;

  @Override
  public final boolean hasNext() {
    if (this.hasNext) {
      if (!this.initialized) {
        init();
      }
      if (this.loadNext) {
        try {
          this.object = getNext();
          this.loadNext = false;
        } catch (final NoSuchElementException e) {
          close();
          this.hasNext = false;
        }
      }
    }
    return this.hasNext;
  }

  public synchronized void init() {
    if (!this.initialized) {
      this.initialized = true;
      doInit();
    }
  }

  @Override
  public Iterator<T> iterator() {
    return this;
  }

  @Override
  public final T next() {
    if (hasNext()) {
      final T currentObject = this.object;
      this.loadNext = true;
      return currentObject;
    } else {
      throw new NoSuchElementException();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  protected void setLoadNext(final boolean loadNext) {
    this.loadNext = loadNext;
    this.hasNext = true;
  }
}
