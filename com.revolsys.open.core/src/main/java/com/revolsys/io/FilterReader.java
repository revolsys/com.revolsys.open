package com.revolsys.io;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.PreDestroy;

import com.revolsys.collection.iterator.FilterIterator;

public class FilterReader<T> extends AbstractReader<T> {

  private Predicate<T> filter;

  private Reader<T> reader;

  public FilterReader(final Predicate<T> filter, final Reader<T> reader) {
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

  protected Predicate<T> getFilter() {
    return this.filter;
  }

  @Override
  public Map<String, Object> getProperties() {
    return this.reader.getProperties();
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
