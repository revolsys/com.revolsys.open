package com.revolsys.io;

import java.io.Closeable;

import com.revolsys.properties.ObjectWithProperties;

public interface Writer<T> extends ObjectWithProperties, Closeable {
  @Override
  void close();

  void flush();

  void open();

  void write(T object);
}
