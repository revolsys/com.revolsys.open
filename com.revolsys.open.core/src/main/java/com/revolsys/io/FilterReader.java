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
    if (this.reader != null) {
      this.reader.close();
    }
    this.filter = null;
    this.reader = null;
  }

  protected Filter<T> getFilter() {
    return this.filter;
  }

  protected Reader<T> getReader() {
    return this.reader;
  }

  @Override
  public Iterator<T> iterator() {
    final Iterator<T> iterator = this.reader.iterator();
    return new FilterIterator<T>(this.filter, iterator);
  }

  @Override
  public void open() {
    this.reader.open();
  }

}
