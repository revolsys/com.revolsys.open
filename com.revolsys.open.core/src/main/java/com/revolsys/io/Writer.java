package com.revolsys.io;

import com.revolsys.properties.ObjectWithProperties;

public interface Writer<T> extends ObjectWithProperties, BaseCloseable {
  @Override
  default void close() {
  }

  default void flush() {
  }

  default void open() {
  }

  void write(T object);
}
