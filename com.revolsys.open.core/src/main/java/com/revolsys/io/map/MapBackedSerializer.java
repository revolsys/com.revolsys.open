package com.revolsys.io.map;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class MapBackedSerializer extends DelegatingMap<String, Object> implements MapSerializer {
  public static MapBackedSerializer hash() {
    return new MapBackedSerializer(new HashMap<String, Object>());
  }

  public static MapBackedSerializer linked() {
    return new MapBackedSerializer(new LinkedHashMap<String, Object>());
  }

  public static MapBackedSerializer tree() {
    return new MapBackedSerializer(new TreeMap<String, Object>());
  }

  public MapBackedSerializer(final Map<String, Object> map) {
    super(map);
  }

  @Override
  public Map<String, Object> toMap() {
    return new LinkedHashMap<>(getMap());
  }
}
