package com.revolsys.gis.graph.attribute;

public abstract class AbstractObjectAttributeProxy<T, O> implements
  ObjectAttributeProxy<T, O> {
  private T value;

  public void clearValue() {
    value = null;
  }

  public abstract T createValue(
    final O object);

  public T getValue(
    final O object) {
    if (value == null) {
      synchronized (this) {
        if (value == null) {
          value = createValue(object);
        }
      }
    }
    return value;
  }
}
