package com.revolsys.io;

import java.util.Iterator;

import javax.annotation.PreDestroy;

import com.revolsys.collection.FilterIterator;
import com.revolsys.filter.Filter;

public class FilterReader<T> extends AbstractReader<T> {

  private Filter<T> filter;

  private Reader<T> reader;

  public FilterReader(final Filter<T> filter, final Reader<T> reader) {
    this.filter = filter;
    this.reader = reader;
  }

  @Override
  @PreDestroy
  public void close() {
    super.close();
    if (reader != null) {
      reader.close();
    }
    filter = null;
    reader = null;
  }

  protected Filter<T> getFilter() {
    return filter;
  }

  protected Reader<T> getReader() {
    return reader;
  }

  @Override
  public Iterator<T> iterator() {
    final Iterator<T> iterator = reader.iterator();
    return new FilterIterator<T>(filter, iterator);
  }

  @Override
  public void open() {
    reader.open();
  }

}
