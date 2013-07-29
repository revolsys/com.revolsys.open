package com.revolsys.io.map;

import java.util.Map;

public interface MapObjectFactory {
  String getDescription();

  String getTypeName();

  <V> V toObject(Map<String, ? extends Object> map);

}
