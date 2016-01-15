package com.revolsys.util;

import com.revolsys.datatype.DataTypes;
import com.revolsys.io.BaseCloseable;

public class ValueCloseable<T> implements BaseCloseable {
  private ValueHolder<T> valueHolder;

  private T currentValue;

  private T originalValue;

  public ValueCloseable(final ValueHolder<T> valueHolder, final T newValue) {
    this.valueHolder = valueHolder;
    this.currentValue = newValue;
    if (valueHolder != null) {
      this.originalValue = valueHolder.setValue(newValue);
    }
  }

  @Override
  public void close() {
    if (this.valueHolder != null) {
      this.currentValue = this.valueHolder.setValue(this.originalValue);
      this.valueHolder = null;
    }
  }

  public T getCurrentValue() {
    return this.currentValue;
  }

  public T getOriginalValue() {
    return this.originalValue;
  }

  @Override
  public String toString() {
    return DataTypes.toString(this.currentValue);
  }
}
