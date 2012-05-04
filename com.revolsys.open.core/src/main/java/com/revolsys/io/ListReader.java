package com.revolsys.io;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ListReader<T> extends AbstractReader<T> {

  private List<T> values = Collections.emptyList();

  public ListReader() {
  }

  public ListReader(final List<T> values) {
    this.values = values;
  }

  @Override
  public void close() {
    values = null;
  }

  public Iterator<T> iterator() {
    return values.iterator();
  }

  public void open() {
  }

}
