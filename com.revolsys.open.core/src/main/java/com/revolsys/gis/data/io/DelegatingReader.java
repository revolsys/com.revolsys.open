package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.Map;

import com.revolsys.collection.AbstractIterator;
import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;

public class DelegatingReader<T> extends AbstractReader<T> {
  private Reader<T> reader;

  private Iterator<T> iterator;

  public DelegatingReader() {
  }

  public DelegatingReader(final Reader<T> reader) {
    this.reader = reader;
  }

  @Override
  public final void close() {
    try {
      if (reader != null) {
        reader.close();
      }
    } finally {
      doClose();
    }
  }

  protected void doClose() {
    if (iterator instanceof AbstractIterator) {
      final AbstractIterator<T> iter = (AbstractIterator<T>)iterator;
      iter.close();
    }
  }

  @Override
  public Map<String, Object> getProperties() {
    return reader.getProperties();
  }

  @SuppressWarnings("unchecked")
  @Override
  public <C> C getProperty(final String name) {
    return (C)reader.getProperty(name);
  }

  public Reader<T> getReader() {
    return reader;
  }

  @Override
  public Iterator<T> iterator() {
    if (iterator == null) {
      iterator = reader.iterator();
    }
    return iterator;
  }

  @Override
  public void open() {
    reader.open();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    reader.setProperty(name, value);
  }

  public void setReader(final Reader<T> reader) {
    this.reader = reader;
  }
}
