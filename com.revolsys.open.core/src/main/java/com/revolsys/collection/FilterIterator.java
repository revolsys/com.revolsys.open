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
    if (iterator instanceof AbstractIterator) {
      final AbstractIterator<T> abstractIterator = (AbstractIterator<T>)iterator;
      abstractIterator.close();
    }
    filter = null;
    iterator = null;
  }

  protected Filter<T> getFilter() {
    return filter;
  }

  protected Iterator<T> getIterator() {
    return iterator;
  }

  @Override
  protected T getNext() throws NoSuchElementException {
    while (iterator != null && iterator.hasNext()) {
      final T value = iterator.next();
      if (filter == null || filter.accept(value)) {
        return value;
      }
    }
    throw new NoSuchElementException();
  }
}
