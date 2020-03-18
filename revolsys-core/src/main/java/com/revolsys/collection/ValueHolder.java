package com.revolsys.collection;

import com.revolsys.util.Emptyable;

public class ValueHolder<T> implements Emptyable {
  private T value;

  public ValueHolder() {
  }

  public ValueHolder(final T value) {
    this.value = value;
  }

  public T getValue() {
    return this.value;
  }

  @Override
  public boolean isEmpty() {
    return this.value == null;
  }

  public void setValue(final T value) {
    this.value = value;
  }
}
