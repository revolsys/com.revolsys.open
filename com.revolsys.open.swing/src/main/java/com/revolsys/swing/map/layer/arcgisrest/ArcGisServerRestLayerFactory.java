package com.revolsys.swing.map.layer.arcgisrest;

import java.util.Map;

import com.revolsys.io.map.AbstractMapObjectFactory;

public class ArcGisServerRestLayerFactory extends AbstractMapObjectFactory {

  public ArcGisServerRestLayerFactory() {
    super("arcgisServerRest", "Arc GIS Server REST");
  }

  @Override
  public <V> V toObject(final Map<String, ? extends Object> properties) {
    final String url = (String)properties.get("url");
    final ArcGisServerRestLayer layer = new ArcGisServerRestLayer(url);
    layer.setProperties(properties);
    return (V)layer;
  }
}
