package com.revolsys.util;

public interface ValueHolder<T> {
  default ValueCloseable<T> closeable(final T value) {
    return new ValueCloseable<T>(this, value);
  }

  T getValue();

  default void run(final T newValue, final Runnable runnable) {
    final T oldValue = setValue(newValue);
    try {
      runnable.run();
    } finally {
      setValue(oldValue);
    }
  }

  T setValue(T value);
}
