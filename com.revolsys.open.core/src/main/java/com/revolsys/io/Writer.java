package com.revolsys.io;

import java.io.Closeable;

import com.revolsys.properties.ObjectWithProperties;

public interface Writer<T> extends ObjectWithProperties, Closeable {
  @Override
  default void close() {
  }

  default void flush() {
  }

  default void open() {
  }

  void write(T object);
}
