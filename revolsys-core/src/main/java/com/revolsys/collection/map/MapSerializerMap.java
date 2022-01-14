package com.revolsys.collection.map;

import java.util.Set;

import com.revolsys.io.map.MapSerializer;

public class MapSerializerMap implements MapEx {

  private final MapSerializer serializer;

  public MapSerializerMap(final MapSerializer serializer) {
    this.serializer = serializer;
  }

  @Override
  public MapEx clone() {
    return this;
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return this.serializer.toMap().entrySet();
  }

}
