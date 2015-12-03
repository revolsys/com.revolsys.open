package com.revolsys.io;

import java.io.Closeable;

@FunctionalInterface
public interface BaseCloseable extends Closeable {
  @Override
  void close();
}
