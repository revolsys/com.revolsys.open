package com.revolsys.io;

import com.revolsys.properties.ObjectWithProperties;

public interface Writer<T> extends ObjectWithProperties, AutoCloseable {
  @Override
  void close() throws RuntimeException;

  void flush();

  void open();

  void write(T object);
}
