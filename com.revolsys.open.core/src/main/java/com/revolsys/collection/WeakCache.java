package com.revolsys.collection;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class WeakCache<K, V> implements Map<K, V> {
  private final Map<K, Reference<V>> cache = new WeakHashMap<K, Reference<V>>();

  private final Map<K, V> map;

  public WeakCache() {
    this(null);
  }

  public WeakCache(final Map<K, V> map) {
    this.map = map;
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @Override
  public boolean containsKey(final Object obj) {
    if (map == null) {
      return cache.containsKey(obj);
    } else {
      return map.containsKey(obj);
    }
  }

  @Override
  public boolean containsValue(final Object value) {
    if (map == null) {
      return cache.containsKey(value);
    } else {
      return map.containsKey(value);
    }
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    if (map == null) {
      return new ReferenceEntrySet<K, V>(cache.entrySet());
    } else {
      throw new UnsupportedOperationException();
    }
  }

  public void evict(final K key) {
    cache.remove(key);
  }

  @Override
  @SuppressWarnings("unchecked")
  public V get(final Object key) {
    V value = null;
    final Reference<V> reference = cache.get(key);
    if (reference != null) {
      value = reference.get();
    }
    if (value == null) {
      if (map != null) {
        value = map.get(key);
      }
      if (value == null) {
        cache.remove(key);
      } else {
        cache.put((K)key, new SoftReference<V>(value));
      }
    }
    return value;
  }

  @Override
  public boolean isEmpty() {
    if (map == null) {
      return cache.isEmpty();
    } else {
      return map.isEmpty();
    }
  }

  @Override
  public Set<K> keySet() {
    if (map == null) {
      return cache.keySet();
    } else {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public V put(final K key, final V value) {
    V oldValue = null;
    if (value == null) {
      final Reference<V> oldReference = cache.remove(key);

      if (map == null) {
        if (oldReference != null) {
          oldValue = oldReference.get();
        }
      } else {
        oldValue = map.remove(key);
      }
    } else {
      final Reference<V> oldReference = cache.put(key, new SoftReference<V>(
        value));
      if (map == null) {
        if (oldReference != null) {
          oldValue = oldReference.get();
        }
      } else {
        oldValue = map.put(key, value);
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
    final Reference<V> oldReference = cache.remove(obj);
    if (map == null) {
      return oldReference.get();
    } else {
      return map.remove(obj);
    }
  }

  @Override
  public int size() {
    if (map == null) {
      return cache.size();
    } else {
      return map.size();
    }
  }

  @Override
  public Collection<V> values() {
    if (map == null) {
      return new ReferenceSet<V>(cache.values());
    } else {
      throw new UnsupportedOperationException();
    }
  }
}
