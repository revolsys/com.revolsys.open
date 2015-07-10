package com.revolsys.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ThreadLocalMap<K, V> implements Map<K, V> {
  private final ThreadLocal<Map<K, V>> map = new ThreadLocal<Map<K, V>>();

  @Override
  public void clear() {
    this.map.set(null);
  }

  @Override
  public boolean containsKey(final Object key) {
    final Map<K, V> localMap = getMap();
    return localMap.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    final Map<K, V> localMap = getMap();
    return localMap.containsValue(value);
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    final Map<K, V> localMap = getMap();
    return localMap.entrySet();
  }

  @Override
  public V get(final Object key) {
    final Map<K, V> localMap = getMap();
    return localMap.get(key);
  }

  public Map<K, V> getMap() {
    Map<K, V> localMap = this.map.get();
    if (localMap == null) {
      localMap = new HashMap<K, V>();
      this.map.set(localMap);
    }
    return localMap;
  }

  @Override
  public boolean isEmpty() {
    final Map<K, V> localMap = this.map.get();
    return localMap == null || localMap.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    final Map<K, V> localMap = getMap();
    return localMap.keySet();
  }

  @Override
  public V put(final K key, final V value) {
    final Map<K, V> localMap = getMap();
    return localMap.put(key, value);
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> t) {
    final Map<K, V> localMap = getMap();
    localMap.putAll(t);
  }

  @Override
  public V remove(final Object key) {
    final Map<K, V> localMap = getMap();
    return localMap.remove(key);
  }

  @Override
  public int size() {
    final Map<K, V> localMap = getMap();
    return localMap.size();
  }

  @Override
  public String toString() {
    final Map<K, V> localMap = this.map.get();
    if (localMap == null) {
      return "{}";
    } else {
      return localMap.toString();
    }
  }

  @Override
  public Collection<V> values() {
    final Map<K, V> localMap = getMap();
    return localMap.values();
  }
}
