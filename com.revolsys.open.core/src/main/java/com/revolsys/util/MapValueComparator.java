package com.revolsys.util;

import java.util.Comparator;
import java.util.Map;

public class MapValueComparator<K extends Comparable<K>, V extends Comparable<V>>
  implements Comparator<K> {

  private Map<K, V> map;

  public MapValueComparator(Map<K, V> map) {
    this.map = map;
  }

  public int compare(K k1, K k2) {
    final V v1 = map.get(k1);
    final V v2 = map.get(k2);
    int compare = v1.compareTo(v2);
    if (compare == 0) {
      return k1.compareTo(k2);
    } else {
      return compare;
    }
  }
}
