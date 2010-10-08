package com.revolsys.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.io.AbstractObjectWithProperties;

public abstract class AbstractIterator<T> extends AbstractObjectWithProperties
  implements Iterator<T> {

  private boolean hasNext = true;

  private boolean initialized;

  private boolean loadNext = true;

  private T object;

  public final void close() {
    hasNext = false;
    object = null;
    doClose();
  }

  protected abstract void doClose();

  protected abstract void doInit();

  @Override
  protected void finalize()
    throws Throwable {
    close();
  }

  protected abstract T getNext()
    throws NoSuchElementException;

  public final boolean hasNext() {
    if (hasNext) {
      if (!initialized) {
        init();
      }
      if (loadNext) {
        try {
          object = getNext();
          loadNext = false;
        } catch (final NoSuchElementException e) {
          hasNext = false;
        }
      }
    }
    return hasNext;
  }

  private void init() {
    initialized = true;
    doInit();
  }

  public final T next() {
    if (hasNext()) {
      final T currentObject = object;
      loadNext = true;
      return currentObject;
    } else {
      throw new NoSuchElementException();
    }
  }

  public void remove() {
    throw new UnsupportedOperationException();
  }
}
