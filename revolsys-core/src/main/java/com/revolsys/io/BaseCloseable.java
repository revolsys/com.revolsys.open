package com.revolsys.io;

import java.io.Closeable;
import java.util.function.Consumer;

@FunctionalInterface
public interface BaseCloseable extends Closeable {
  static Consumer<BaseCloseable> CLOSER = BaseCloseable::close;

  static <C extends BaseCloseable> Consumer<? super C> closer() {
    return CLOSER;
  }

  @Override
  void close();

  default BaseCloseable wrap() {
    return new CloseableWrapper(this);
  }
}
