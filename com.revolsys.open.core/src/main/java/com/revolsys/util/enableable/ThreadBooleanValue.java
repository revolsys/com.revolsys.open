package com.revolsys.util.enableable;

public final class ThreadBooleanValue implements BooleanValue {
  private final ThreadLocal<Boolean> threadValue = new ThreadLocal<>();

  private boolean defaultValue = true;

  public ThreadBooleanValue() {
  }

  public ThreadBooleanValue(final boolean defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  public boolean getValue() {
    final Boolean value = this.threadValue.get();
    if (value == null) {
      return this.defaultValue;
    } else {
      return value;
    }
  }

  @Override
  public boolean setValue(final boolean value) {
    final boolean oldValue = getValue();
    if (value == this.defaultValue) {
      this.threadValue.set(null);
    } else {
      this.threadValue.set(value);
    }
    return oldValue;
  }

  @Override
  public String toString() {
    return Boolean.toString(getValue());
  }
}
