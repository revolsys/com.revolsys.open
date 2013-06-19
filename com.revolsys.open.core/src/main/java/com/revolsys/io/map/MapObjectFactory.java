package com.revolsys.io.map;

import java.util.Map;

public interface MapObjectFactory {
  <V> V toObject(Map<String, ? extends Object> map);
}
