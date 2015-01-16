package com.revolsys.util;

import java.util.HashMap;
import java.util.Map;

public class ValueCache<T> {
  private final Map<T, T> cache = new HashMap<T, T>();

  private boolean readOnly = false;

  public void addValue(final T value) {
    if (!this.readOnly && !this.cache.containsKey(value)) {
      this.cache.put(value, value);
    }
  }

  public T getValue(final T value) {
    final T cachedValue = this.cache.get(value);
    if (cachedValue == null) {
      return value;
    } else {
      return cachedValue;
    }
  }

  public boolean isReadOnly() {
    return this.readOnly;
  }

  public void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

}
