package com.revolsys.factory;

import java.util.Map;
import java.util.TreeMap;

public class TreeMapFactory<K, V> implements Factory<Map<K, V>> {
  @SuppressWarnings("rawtypes")
  private static final TreeMapFactory INSTANCE = new TreeMapFactory<>();

  @SuppressWarnings("unchecked")
  public static <K1, V1> TreeMapFactory<K1, V1> get() {
    return INSTANCE;
  }

  @Override
  public Map<K, V> create() {
    return new TreeMap<>();
  }
}
