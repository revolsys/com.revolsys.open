package com.revolsys.gis.model.data.equals;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class MapEquals implements Equals<Map<String, Object>> {
  private EqualsRegistry equalsRegistry;

  @Override
  public boolean equals(Map<String, Object> map1, Map<String, Object> map2,
    final Collection<String> exclude) {
    if (map1 == null) {
      map1 = Collections.emptyMap();
    }
    if (map2 == null) {
      map2 = Collections.emptyMap();
    }
    final Set<String> keys = new TreeSet<String>();
    keys.addAll(map1.keySet());
    keys.addAll(map2.keySet());
    keys.removeAll(exclude);

    for (final String key : keys) {
      final Object value1 = map1.get(key);
      final Object value2 = map2.get(key);
      if (!equalsRegistry.equals(value1, value2, exclude)) {
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
