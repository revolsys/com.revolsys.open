package com.revolsys.swing.map.layer.bing;

import java.util.Map;

import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.swing.map.layer.AbstractLayerFactory;

public class BingLayerFactory extends AbstractLayerFactory<BingLayer> {

  public BingLayerFactory() {
    super("bing", "Bing Tiles");
  }

  @Override
  public BingLayer createLayer(final Map<String, Object> properties) {
    ImagerySet imagerySet = ImagerySet.Road;
    final String imagerySetName = (String)properties.remove("imagerySet");
    if (StringUtils.hasText(imagerySetName)) {
      try {
        imagerySet = ImagerySet.valueOf(imagerySetName);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(BingLayer.class).error(
          "Unknown Bing imagery set " + imagerySetName, e);
      }
    }
    MapLayer mapLayer = null;
    final String mapLayerName = (String)properties.remove("mapLayer");
    if (StringUtils.hasText(mapLayerName)) {
      try {
        mapLayer = MapLayer.valueOf(mapLayerName);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(BingLayer.class).error(
          "Unknown Bing map layer " + mapLayerName, e);
      }
    }
    final BingLayer layer = new BingLayer(imagerySet, mapLayer);
    layer.setProperties(properties);
    return layer;
  }
}
