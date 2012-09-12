package com.revolsys.collection;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.collection.bplus.BPlusTreeMap;

public class MapKeySetEntrySet<K, V> extends AbstractSet<Entry<K, V>> {
  private Map<K, V> map;

  public MapKeySetEntrySet(Map<K, V> map) {
    this.map = map;
  }

  @Override
  public void clear() {
    map.clear();
  }

  /**
   * @see java.util.AbstractCollection#contains(java.lang.Object)
   */
  @Override
  public boolean contains(final Object o) {
    if (o instanceof BPlusTreeMap.Entry) {
      @SuppressWarnings("unchecked")
      final Entry<K, V> e = (Entry<K, V>)o;
      final Entry<K, V> candidate = new MapKeyEntry<K, V>(map, e.getKey());
      return candidate != null && candidate.equals(e);
    } else {
      return false;
    }
  }

  @Override
  public Iterator<Map.Entry<K, V>> iterator() {
    return new MapKeySetEntryIterator<K, V>(map);
  }

  @Override
  public boolean remove(final Object o) {
    return map.remove(o) != null;
  }

  @Override
  public int size() {
    return map.size();
  }
}
