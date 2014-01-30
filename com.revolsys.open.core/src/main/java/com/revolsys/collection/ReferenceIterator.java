package com.revolsys.collection;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ReferenceIterator<V> extends AbstractIterator<V> {
  private Iterator<Reference<V>> iterator;

  public ReferenceIterator(final Collection<Reference<V>> collection) {
    this.iterator = collection.iterator();
  }

  @Override
  protected void doClose() {
    super.doClose();
    iterator = null;
  }

  @Override
  protected V getNext() throws NoSuchElementException {
    while (iterator.hasNext()) {
      final Reference<V> reference = iterator.next();
      final V value = reference.get();
      if (value != null) {
        return value;
      }
    }
    throw new NoSuchElementException();
  }
}
