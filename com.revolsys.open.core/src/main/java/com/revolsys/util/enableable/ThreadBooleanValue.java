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
  public Boolean getValue() {
    final Boolean value = this.threadValue.get();
    if (value == null) {
      return this.defaultValue;
    } else {
      return value;
    }
  }

  @Override
  public Boolean setValue(final Boolean value) {
    final boolean oldValue = getValue();
    final boolean booleanValue = value == Boolean.TRUE;
    if (booleanValue == this.defaultValue) {
      this.threadValue.set(null);
    } else {
      this.threadValue.set(booleanValue);
    }
    return oldValue;
  }

  @Override
  public String toString() {
    final Boolean value = getValue();
    return value.toString();
  }
}
