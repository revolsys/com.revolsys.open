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

  public WeakCache(Map<K, V> map) {
    this.map = map;
  }

  @SuppressWarnings("unchecked")
  public V get(Object key) {
    V value = null;
    Reference<V> reference = cache.get(key);
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

  public V put(K key, V value) {
    V oldValue = null;
    if (value == null) {
      Reference<V> oldReference = cache.remove(key);

      if (map == null) {
        if (oldReference != null) {
          oldValue = oldReference.get();
        }
      } else {
        oldValue = map.remove(key);
      }
    } else {
      Reference<V> oldReference = cache.put(key, new SoftReference<V>(value));
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

  public void evict(K key) {
    cache.remove(key);
  }

  public void clear() {
    cache.clear();
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
  public boolean isEmpty() {
    if (map == null) {
      return cache.isEmpty();
    } else {
      return map.isEmpty();
    }
  }

  @Override
  public boolean containsKey(Object obj) {
    if (map == null) {
      return cache.containsKey(obj);
    } else {
      return map.containsKey(obj);
    }
  }

  @Override
  public boolean containsValue(Object value) {
    if (map == null) {
      return cache.containsKey(value);
    } else {
      return map.containsKey(value);
    }
  }

  @Override
  public V remove(Object obj) {
    Reference<V> oldReference = cache.remove(obj);
    if (map == null) {
      return oldReference.get();
    } else {
      return map.remove(obj);
    }
  }

  @Override
  public void putAll(Map<? extends K, ? extends V> map) {
    for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
      K key = entry.getKey();
      V value = entry.getValue();
      put(key, value);
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
  public Collection<V> values() {
    if (map == null) {
      return new ReferenceSet<V>(cache.values());
    } else {
      throw new UnsupportedOperationException();
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
}
