package com.revolsys.util.enableable;

import java.io.Closeable;

public class BooleanValueCloseable implements Closeable {
  private BooleanValue booleanValue;

  private boolean currentValue;

  private boolean originalValue;

  public BooleanValueCloseable(final BooleanValue booleanValue, final boolean newValue) {
    this.booleanValue = booleanValue;
    this.currentValue = newValue;
    if (booleanValue != null) {
      this.originalValue = booleanValue.setValue(newValue);
    }
  }

  @Override
  public void close() {
    if (this.booleanValue != null) {
      this.currentValue = this.booleanValue.setValue(this.originalValue);
      this.booleanValue = null;
    }
  }

  public boolean isFalse() {
    return !this.currentValue;
  }

  public boolean isTrue() {
    return this.currentValue;
  }

  @Override
  public String toString() {
    return Boolean.toString(this.currentValue);
  }
}
