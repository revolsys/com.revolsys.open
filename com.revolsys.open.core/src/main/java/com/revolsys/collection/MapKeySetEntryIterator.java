package com.revolsys.collection;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MapKeySetEntryIterator<K, V> implements Iterator<Entry<K, V>> {
  private Map<K, V> map;

  private Iterator<K> keyInterator;

  public MapKeySetEntryIterator(Map<K, V> map) {
    this.map = map;
    this.keyInterator = map.keySet().iterator();
  }

  @Override
  public boolean hasNext() {
    return keyInterator.hasNext();
  }

  @Override
  public Entry<K, V> next() {
    K key = keyInterator.next();
    return new MapKeyEntry<K,V>(map, key);
  }

  @Override
  public void remove() {
    keyInterator.remove();
  }
  
  
}
