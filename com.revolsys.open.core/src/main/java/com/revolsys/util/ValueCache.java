package com.revolsys.util;

import java.util.HashMap;
import java.util.Map;

public class ValueCache<T> {
  private final Map<T, T> cache = new HashMap<T, T>();

  private boolean readOnly = false;

  public void addValue(final T value) {
    if (!readOnly && !cache.containsKey(value)) {
      cache.put(value, value);
    }
  }

  public T getValue(final T value) {
    final T cachedValue = cache.get(value);
    if (cachedValue == null) {
      return value;
    } else {
      return cachedValue;
    }
  }

  public boolean isReadOnly() {
    return readOnly;
  }

  public void setReadOnly(final boolean readOnly) {
    this.readOnly = readOnly;
  }

}
