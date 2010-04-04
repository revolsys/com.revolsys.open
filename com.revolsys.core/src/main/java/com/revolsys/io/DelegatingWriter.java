package com.revolsys.io;

import java.util.Map;

import javax.xml.namespace.QName;

public class DelegatingWriter<T> {
  private final Writer writer;

  public DelegatingWriter(
    final Writer writer) {
    this.writer = writer;
  }

  public void close() {
    writer.close();
  }

  public void flush() {
    writer.flush();
  }

  public Map<QName, Object> getProperties() {
    return writer.getProperties();
  }

  public <V> V getProperty(
    final QName name) {
    return (V)writer.getProperty(name);
  }

  public void setProperty(
    final QName name,
    final Object value) {
    writer.setProperty(name, value);
  }

  public void write(
    final T object) {
    writer.write(object);
  }
}
