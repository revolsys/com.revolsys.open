package com.revolsys.collection;

public class ValueHolder<T> {
  private T value;

  public ValueHolder() {
  }

  public ValueHolder(final T value) {
    this.value = value;
  }

  public T getValue() {
    return this.value;
  }

  public void setValue(final T value) {
    this.value = value;
  }
}
