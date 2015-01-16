package com.revolsys.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.revolsys.filter.Filter;

public class FilterIterator<T> extends AbstractIterator<T> {

  private Filter<T> filter;

  private Iterator<T> iterator;

  public FilterIterator(final Filter<T> filter, final Iterator<T> iterator) {
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

  protected Filter<T> getFilter() {
    return this.filter;
  }

  protected Iterator<T> getIterator() {
    return this.iterator;
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    while (this.iterator != null && this.iterator.hasNext()) {
      final T value = this.iterator.next();
      if (this.filter == null || this.filter.accept(value)) {
        return value;
      }
    }
    throw new NoSuchElementException();
  }
}
