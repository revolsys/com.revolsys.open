package com.revolsys.swing.map.layer.openstreetmap;

import java.util.Map;

import com.revolsys.swing.Icons;
import com.revolsys.swing.map.layer.webmercatortilecache.WebMercatorTileCacheLayer;

public class OpenStreetMapLayer extends WebMercatorTileCacheLayer {
  public OpenStreetMapLayer() {
    setType("openStreetMap");
    setIcon(Icons.getIcon("openStreetMap"));
  }

  public OpenStreetMapLayer(final Map<String, ? extends Object> properties) {
    this();
    setProperties(properties);
  }
}
