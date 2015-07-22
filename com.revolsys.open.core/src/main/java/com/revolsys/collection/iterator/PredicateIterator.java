package com.revolsys.collection.iterator;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class PredicateIterator<T> extends AbstractIterator<T> {

  private Predicate<T> filter;

  private Iterator<T> iterator;

  public PredicateIterator(final Predicate<T> filter, final Iterator<T> iterator) {
    this.filter = filter;
    this.iterator = iterator;
  }

  @Override
  protected void doClose() {
    super.doClose();
    if (this.iterator instanceof AbstractIterator) {
      final AbstractIterator<T> abstractIterator = (AbstractIterator<T>)this.iterator;
      abstractIterator.close();
    }
    this.filter = null;
    this.iterator = null;
  }

  protected Predicate<T> getFilter() {
    return this.filter;
  }

  protected Iterator<T> getIterator() {
    return this.iterator;
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    while (this.iterator != null && this.iterator.hasNext()) {
      final T value = this.iterator.next();
      if (this.filter == null || this.filter.test(value)) {
        return value;
      }
    }
    throw new NoSuchElementException();
  }
}
