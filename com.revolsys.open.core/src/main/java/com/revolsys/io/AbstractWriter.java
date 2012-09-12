package com.revolsys.io;

public abstract class AbstractWriter<T> extends AbstractObjectWithProperties
  implements Writer<T> {

  @Override
  public void close() {
  }

  @Override
  public void flush() {
  }
}
