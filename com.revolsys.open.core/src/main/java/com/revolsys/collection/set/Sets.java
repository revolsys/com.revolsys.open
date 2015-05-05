package com.revolsys.collection.set;

import java.util.LinkedHashSet;
import java.util.TreeSet;

public class Sets {
  public static <V> LinkedHashSet<V> linkedHash(final V value) {
    final LinkedHashSet<V> set = new LinkedHashSet<>();
    if (value != null) {
      set.add(value);
    }
    return set;
  }

  public static <V> TreeSet<V> tree(final V value) {
    final TreeSet<V> set = new TreeSet<>();
    if (value != null) {
      set.add(value);
    }
    return set;
  }
}
