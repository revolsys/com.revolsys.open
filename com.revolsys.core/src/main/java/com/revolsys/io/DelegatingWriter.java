package com.revolsys.io;

import java.util.Map;

public class DelegatingWriter<T> extends AbstractWriter<T> {
  private final Writer<T> writer;

  public DelegatingWriter(
    final Writer<T> writer) {
    this.writer = writer;
  }

  public void close() {
    writer.close();
  }

  public void flush() {
    writer.flush();
  }

  public Map<String, Object> getProperties() {
    return writer.getProperties();
  }

  public void write(
    final T object) {
    writer.write(object);
  }
}
