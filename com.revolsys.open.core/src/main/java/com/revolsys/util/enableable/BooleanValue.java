package com.revolsys.util.enableable;

public interface BooleanValue {
  default BooleanValueCloseable closeable(final boolean value) {
    return new BooleanValueCloseable(this, value);
  }

  boolean getValue();

  default boolean isFalse() {
    return !isTrue();
  }

  default boolean isTrue() {
    return getValue();
  }

  default void run(final boolean newValue, final Runnable runnable) {
    final boolean oldValue = setValue(newValue);
    try {
      runnable.run();
    } finally {
      setValue(oldValue);
    }
  }

  boolean setValue(boolean value);
}
