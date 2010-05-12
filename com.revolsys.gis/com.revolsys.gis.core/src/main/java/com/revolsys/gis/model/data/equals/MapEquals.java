package com.revolsys.gis.model.data.equals;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class MapEquals implements Equals<Map<String, Object>> {
  private EqualsRegistry equalsRegistry;

  public boolean equals(
    final Map<String, Object> map1,
    final Map<String, Object> map2,
    final Collection<String> exclude) {
    if (map1 == null) {
      if (map2 == null) {
        return true;
      } else {
        return false;
      }
    } else if (map2 == null) {
      return false;
    } else {
      final Set<String> keys1 = map1.keySet();
      final Set<String> keys2 = map2.keySet();
      for (String key : keys1) {
        if (!keys2.contains(key) && !exclude.contains(key)) {
          return false;
        }
      }
      for (String key : keys2) {
        if (!keys1.contains(key) && !exclude.contains(key)) {
          return false;
        }
      }
    }
    for (final Entry<String, Object> entry : map1.entrySet()) {
      final String key = entry.getKey();
      if (!exclude.contains(key)) {
        final Object value1 = entry.getValue();
        final Object value2 = map2.get(key);
        if (!equalsRegistry.equals(value1, value2, exclude)) {
          return false;
        }
      }
    }
    return true;
  }

  public void setEqualsRegistry(
    final EqualsRegistry equalsRegistry) {
    this.equalsRegistry = equalsRegistry;
  }
}
