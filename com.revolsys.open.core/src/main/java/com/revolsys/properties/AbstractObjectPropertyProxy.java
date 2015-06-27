package com.revolsys.properties;

public abstract class AbstractObjectPropertyProxy<T, O> implements ObjectPropertyProxy<T, O> {
  private transient T value;

  @Override
  public void clearValue() {
    this.value = null;
  }

  public abstract T createValue(final O object);

  @Override
  public T getValue(final O object) {
    if (this.value == null) {
      synchronized (this) {
        if (this.value == null) {
          this.value = createValue(object);
        }
      }
    }
    return this.value;
  }
}
