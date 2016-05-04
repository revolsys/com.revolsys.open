package com.revolsys.webservice;

import com.revolsys.collection.Parent;
import com.revolsys.io.map.MapSerializer;

public interface WebService<V> extends MapSerializer, Parent<V> {
  String getName();

  default boolean isClosed() {
    return false;
  }

  void setName(String name);
}
