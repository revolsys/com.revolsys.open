package com.revolsys.value;

import com.revolsys.io.BaseCloseable;

public interface ValueHolder<T> {
  default BaseCloseable closeable(final T value) {
    return new ValueCloseable<>(this, value);
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
