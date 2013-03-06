package com.revolsys.swing.map.layer;

import java.util.Map;

public interface LayerFactory<T extends Layer> {

  T createLayer(Map<String, Object> properties);
  
  String getTypeName();
  
  String getDescription();
}
