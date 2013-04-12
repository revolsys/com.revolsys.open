package com.revolsys.collection;

import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.collection.bplus.BPlusTreeMap;

public class MapKeyEntry<K, V> implements Entry<K, V> {
  private Map<K, V> map;

  private final K key;

  public MapKeyEntry(final Map<K, V> map, final K key) {
    this.key = key;
  }

  @Override
  public boolean equals(final Object o) {
    if (o instanceof BPlusTreeMap.Entry) {
      @SuppressWarnings("unchecked")
      final Entry<K, V> e = (Entry<K, V>)o;
      final K k1 = getKey();
      final K k2 = e.getKey();
      if (k1 == k2) {
        final Object v1 = getValue();
        final Object v2 = e.getValue();
        if (v1 == v2 || (v1 != null && v1.equals(v2))) {
          return true;
        }
      }
    }
    return false;

  }

  @Override
  public K getKey() {
    return key;
  }

  @Override
  public V getValue() {
    return map.get(key);
  }

  @Override
  public int hashCode() {
    return key.hashCode();
  }

  @Override
  public V setValue(final V newValue) {
    final V oldValue = map.get(key);
    map.put(key, newValue);
    return oldValue;
  }

  @Override
  public String toString() {
    return getKey() + "=" + getValue();
  }
}
