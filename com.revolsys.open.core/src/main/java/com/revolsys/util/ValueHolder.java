package com.revolsys.util;

public class ValueHolder<R> implements ValueWrapper<R> {

  protected R value;

  @Override
  public void close() {
    this.value = null;
  }

  @Override
  public synchronized R getValue() {
    return this.value;
  }
}
