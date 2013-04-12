package com.revolsys.collection;

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
    return keyInterator.hasNext();
  }

  @Override
  public Entry<K, V> next() {
    final K key = keyInterator.next();
    return new MapKeyEntry<K, V>(map, key);
  }

  @Override
  public void remove() {
    keyInterator.remove();
  }

}
