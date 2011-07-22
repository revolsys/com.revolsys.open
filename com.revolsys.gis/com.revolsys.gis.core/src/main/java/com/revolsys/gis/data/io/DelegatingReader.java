package com.revolsys.gis.data.io;

import java.util.Iterator;
import java.util.Map;

import com.revolsys.io.AbstractReader;
import com.revolsys.io.Reader;

public class DelegatingReader<T> extends AbstractReader<T> {
  private Reader<T> reader;

  public DelegatingReader() {
  }

  protected Reader<T> getReader() {
    return reader;
  }

  protected void setReader(
    Reader<T> reader) {
    this.reader = reader;
  }

  public DelegatingReader(
    final Reader<T> reader) {
    this.reader = reader;
  }

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
  }

  public void open() {
    reader.open();
  }

  @Override
  public Map<String, Object> getProperties() {
    return reader.getProperties();
  }

  @Override
  public <C> C getProperty(
    final String name) {
    return (C)reader.getProperty(name);
  }

  public Iterator<T> iterator() {
    return reader.iterator();
  }

  @Override
  public void setProperty(
    final String name,
    final Object value) {
    reader.setProperty(name, value);
  }
}
