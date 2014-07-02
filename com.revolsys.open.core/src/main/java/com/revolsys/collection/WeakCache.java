package com.revolsys.collection;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakCache<K, V> implements Map<K, V> {
  private final Map<K, Reference<V>> cache = new WeakHashMap<>();

  public WeakCache() {
  }

  @Override
  public void clear() {
    this.cache.clear();
  }

  @Override
  public boolean containsKey(final Object key) {
    return this.cache.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return this.cache.containsKey(value);
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return new ReferenceEntrySet<K, V>(this.cache.entrySet());
  }

  public void evict(final K key) {
    this.cache.remove(key);
  }

  @Override
  public V get(final Object key) {
    V value = null;
    final Reference<V> reference = this.cache.get(key);
    if (reference != null) {
      value = reference.get();
    }
    if (value == null) {
      this.cache.remove(key);
    }
    return value;
  }

  @Override
  public boolean isEmpty() {
    return this.cache.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return this.cache.keySet();
  }

  @Override
  public V put(final K key, final V value) {
    V oldValue = null;
    if (value == null) {
      final Reference<V> oldReference = this.cache.remove(key);

      if (oldReference != null) {
        oldValue = oldReference.get();
      }
    } else {
      final Reference<V> oldReference = this.cache.put(key,
        new WeakReference<V>(value));
      if (oldReference != null) {
        oldValue = oldReference.get();
      }
    }
    return oldValue;
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> map) {
    for (final Entry<? extends K, ? extends V> entry : map.entrySet()) {
      final K key = entry.getKey();
      final V value = entry.getValue();
      put(key, value);
    }
  }

  @Override
  public V remove(final Object obj) {
    final Reference<V> oldReference = this.cache.remove(obj);
    return oldReference.get();
  }

  @Override
  public int size() {
    return this.cache.size();
  }

  @Override
  public Collection<V> values() {
    return new ReferenceSet<V>(this.cache.values());
  }
}
