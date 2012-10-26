package com.revolsys.collection;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class LruMap<K, V> extends LinkedHashMap<K, V> {
  private static final long serialVersionUID = 1L;

  private int maxSize;

  public LruMap(int maxSize) {
    super(maxSize, 0.75f, true);
    this.maxSize = maxSize;
  }

  protected boolean removeEldestEntry(Entry<K, V> eldest) {
    return size() > maxSize;
  }
}
