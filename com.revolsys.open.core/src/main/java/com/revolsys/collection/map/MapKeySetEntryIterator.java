package com.revolsys.collection.map;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MapKeySetEntryIterator<K, V> implements Iterator<Entry<K, V>> {
  private final Map<K, V> map;

  private final Iterator<K> keyInterator;

  public MapKeySetEntryIterator(final Map<K, V> map) {
    this.map = map;
    this.keyInterator = map.keySet().iterator();
  }

  @Override
  public boolean hasNext() {
    return this.keyInterator.hasNext();
  }

  @Override
  public Entry<K, V> next() {
    final K key = this.keyInterator.next();
    return new MapKeyEntry<K, V>(this.map, key);
  }

  @Override
  public void remove() {
    this.keyInterator.remove();
  }

}
