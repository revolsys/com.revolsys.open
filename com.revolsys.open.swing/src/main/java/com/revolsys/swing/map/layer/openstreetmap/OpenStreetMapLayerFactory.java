package com.revolsys.swing.map.layer.openstreetmap;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.swing.map.layer.AbstractLayerFactory;

public class OpenStreetMapLayerFactory extends
  AbstractLayerFactory<OpenStreetMapLayer> {

  public OpenStreetMapLayerFactory() {
    super("openStreetMap", "Open Street Map Tiles");
  }

  @Override
  public OpenStreetMapLayer createLayer(final Map<String, Object> properties) {
    OpenStreetMapClient client;
    final String serverUrl = (String)properties.remove("url");
    if (StringUtils.hasText(serverUrl)) {
      client = new OpenStreetMapClient(serverUrl);
    } else {
      client = new OpenStreetMapClient();
    }
    final OpenStreetMapLayer layer = new OpenStreetMapLayer(client);
    layer.setProperties(properties);
    return layer;
  }
}
