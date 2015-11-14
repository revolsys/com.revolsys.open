package com.revolsys.util.enableable;

public class GlobalBooleanValue implements BooleanValue {

  private boolean value = true;

  public GlobalBooleanValue(final boolean value) {
    this.value = value;
  }

  @Override
  public synchronized boolean getValue() {
    return this.value;
  }

  @Override
  public synchronized boolean setValue(final boolean value) {
    final boolean oldValue = this.value;
    this.value = value;
    return oldValue;
  }

  @Override
  public String toString() {
    return Boolean.toString(this.value);
  }
}
