package com.revolsys.gis.model.data.equals;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

public class MapEquals implements Equals<Map<String, Object>> {
  private EqualsRegistry equalsRegistry;

  public boolean equals(
    final Map<String, Object> map1,
    final Map<String, Object> map2,
    final Collection<String> exclude) {
    if (!map1.keySet().equals(map2.keySet())) {
      return false;
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
