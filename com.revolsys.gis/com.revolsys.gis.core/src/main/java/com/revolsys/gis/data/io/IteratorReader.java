package com.revolsys.gis.data.io;

import java.util.Iterator;


public class IteratorReader<T> extends AbstractReader<T> {

  private Iterator<T> iterator;

  public IteratorReader(
    final Iterator<T> iterator) {
    this.iterator = iterator;
  }

  public void close() {
    iterator = null;
  }

  public Iterator<T> iterator() {
    return iterator;
  }
}
