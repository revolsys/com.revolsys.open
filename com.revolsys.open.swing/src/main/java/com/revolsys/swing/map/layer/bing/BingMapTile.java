package com.revolsys.swing.map.layer.bing;

import java.awt.Image;

import com.revolsys.gis.bing.BingClient;
import com.revolsys.gis.bing.ImagerySet;
import com.revolsys.gis.bing.MapLayer;
import com.revolsys.swing.map.layer.MapTile;

public class BingMapTile extends MapTile {

  private final String quadKey;

  private final BingLayer layer;

  public BingMapTile(final BingLayer layer, final int zoomLevel,
    final int tileX, final int tileY) {
    super(BingClient.getBoundingBox(zoomLevel, tileX, tileY), 256, 256);
    this.layer = layer;
    this.quadKey = BingClient.getQuadKey(zoomLevel, tileX, tileY);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof BingMapTile) {
      final BingMapTile tile = (BingMapTile)obj;
      if (tile.layer == layer) {
        if (tile.quadKey.equals(quadKey)) {
          return true;
        }
      }
    }
    return false;
  }

  public String getQuadKey() {
    return quadKey;
  }

  @Override
  public int hashCode() {
    return quadKey.hashCode();
  }

  @Override
  public Image loadImage() {
    final BingClient client = layer.getClient();
    final ImagerySet imagerySet = layer.getImagerySet();
    final MapLayer mapLayer = layer.getMapLayer();
    final Image image = client.getMapImage(imagerySet, mapLayer, quadKey);
    return image;
  }

  @Override
  public String toString() {
    return layer + " " + quadKey;
  }
}
