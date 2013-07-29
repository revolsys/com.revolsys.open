package com.revolsys.swing.map.layer.openstreetmap;

import java.util.Map;

import org.springframework.util.StringUtils;

import com.revolsys.io.map.AbstractMapObjectFactory;

public class OpenStreetMapLayerFactory extends AbstractMapObjectFactory {

  public OpenStreetMapLayerFactory() {
    super("openStreetMap", "Open Street Map Tiles");
  }

  @Override
  public <V> V toObject(final Map<String, ? extends Object> properties) {
    OpenStreetMapClient client;
    final String serverUrl = (String)properties.remove("url");
    if (StringUtils.hasText(serverUrl)) {
      client = new OpenStreetMapClient(serverUrl);
    } else {
      client = new OpenStreetMapClient();
    }
    final OpenStreetMapLayer layer = new OpenStreetMapLayer(client);
    layer.setProperties(properties);
    return (V)layer;
  }
}
