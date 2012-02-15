package com.revolsys.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ThreadLocalMap<K, V> implements Map<K, V> {
  private final ThreadLocal<Map<K, V>> map = new ThreadLocal<Map<K, V>>();

  public void clear() {
    final Map<K, V> localMap = getMap();
    localMap.clear();
  }

  public boolean containsKey(final Object key) {
    final Map<K, V> localMap = getMap();
    return localMap.containsKey(key);
  }

  public boolean containsValue(final Object value) {
    final Map<K, V> localMap = getMap();
    return localMap.containsValue(value);
  }

  public Set<Map.Entry<K, V>> entrySet() {
    final Map<K, V> localMap = getMap();
    return localMap.entrySet();
  }

  public V get(final Object key) {
    final Map<K, V> localMap = getMap();
    return localMap.get(key);
  }

  public Map<K, V> getMap() {
    Map<K, V> localMap = map.get();
    if (localMap == null) {
      localMap = new HashMap<K, V>();
      map.set(localMap);
    }
    return localMap;
  }

  public boolean isEmpty() {
    final Map<K, V> localMap = getMap();
    return localMap.isEmpty();
  }

  public Set<K> keySet() {
    final Map<K, V> localMap = getMap();
    return localMap.keySet();
  }

  public V put(final K key, final V value) {
    final Map<K, V> localMap = getMap();
    return localMap.put(key, value);
  }

  public void putAll(final Map<? extends K, ? extends V> t) {
    final Map<K, V> localMap = getMap();
    localMap.putAll(t);
  }

  public V remove(final Object key) {
    final Map<K, V> localMap = getMap();
    return localMap.remove(key);
  }

  public int size() {
    final Map<K, V> localMap = getMap();
    return localMap.size();
  }

  public Collection<V> values() {
    final Map<K, V> localMap = getMap();
    return localMap.values();
  }
}
