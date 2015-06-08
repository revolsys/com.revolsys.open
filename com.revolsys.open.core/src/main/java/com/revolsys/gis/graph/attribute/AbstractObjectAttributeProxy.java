package com.revolsys.gis.graph.attribute;

public abstract class AbstractObjectAttributeProxy<T, O> implements ObjectAttributeProxy<T, O> {
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
