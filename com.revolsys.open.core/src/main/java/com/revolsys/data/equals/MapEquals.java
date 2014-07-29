package com.revolsys.data.equals;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MapEquals implements Equals<Map<Object, Object>> {
  public static boolean equalMap1Keys(final Map<String, Object> map1,
    final Map<String, Object> map2) {
    if (map1 == null) {
      return false;
    } else if (map2 == null) {
      return false;
    } else {
      for (final String key : map1.keySet()) {
        if (!MapEquals.equals(map1, map2, key)) {
          return false;
        }
      }

      return true;
    }
  }

  public static boolean equals(final Map<String, Object> map1,
    final Map<String, Object> map2, final String attributeName) {
    final Object value1 = map1.get(attributeName);
    final Object value2 = map2.get(attributeName);
    return EqualsInstance.INSTANCE.equals(value1, value2);
  }

  private EqualsRegistry equalsRegistry;

  @Override
  public boolean equals(Map<Object, Object> map1, Map<Object, Object> map2,
    final Collection<String> exclude) {
    if (map1 == null) {
      map1 = Collections.emptyMap();
    }
    if (map2 == null) {
      map2 = Collections.emptyMap();
    }
    final Set<Object> keys = new TreeSet<>();
    keys.addAll(map1.keySet());
    keys.addAll(map2.keySet());
    keys.removeAll(exclude);

    for (final Object key : keys) {
      final Object value1 = map1.get(key);
      final Object value2 = map2.get(key);
      if (!this.equalsRegistry.equals(value1, value2, exclude)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void setEqualsRegistry(final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = equalsRegistry;
  }
}
