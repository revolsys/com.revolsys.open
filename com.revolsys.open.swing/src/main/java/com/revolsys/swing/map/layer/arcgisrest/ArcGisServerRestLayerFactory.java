package com.revolsys.swing.map.layer.arcgisrest;

import java.util.Map;

import com.revolsys.swing.map.layer.AbstractLayerFactory;
import com.revolsys.swing.map.layer.Layer;

public class ArcGisServerRestLayerFactory extends AbstractLayerFactory<Layer> {

  public ArcGisServerRestLayerFactory() {
    super("arcgisServerRest", "Arc GIS Server REST");
  }

  @Override
  public ArcGisServerRestLayer createLayer(final Map<String, Object> properties) {
    final String url = (String)properties.get("url");
    final ArcGisServerRestLayer layer = new ArcGisServerRestLayer(url);
    layer.setProperties(properties);
    return layer;
  }
}
