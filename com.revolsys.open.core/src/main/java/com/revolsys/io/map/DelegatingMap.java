package com.revolsys.io.map;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

public class DelegatingMap<K, V> extends AbstractMap<K, V> {

  public static <K2, V2> Map<K2, V2> create(final Map<K2, V2> map) {
    return new DelegatingMap<>(map);
  }

  private Map<K, V> map;

  public DelegatingMap(final Map<K, V> map) {
    super();
    this.map = map;
  }

  @Override
  public void clear() {
    this.map.clear();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return this.map.entrySet();
  }

  @Override
  public V get(final Object key) {
    return this.map.get(key);
  }

  public Map<K, V> getMap() {
    return this.map;
  }

  @Override
  public V put(final K key, final V value) {
    return this.map.put(key, value);
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    this.map.putAll(m);
  }

  @Override
  public V remove(final Object key) {
    return this.map.remove(key);
  }

  public void setMap(final Map<K, V> map) {
    this.map = map;
  }
}
