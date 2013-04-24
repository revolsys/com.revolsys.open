package com.revolsys.swing.map.layer.bing;

import java.awt.Image;

import com.revolsys.gis.bing.BingClient;
import com.revolsys.gis.bing.ImagerySet;
import com.revolsys.gis.bing.MapLayer;
import com.revolsys.swing.map.layer.MapTile;

public class BingMapTile extends MapTile {

  private String quadKey;

  private BingLayer layer;

  public BingMapTile(BingLayer layer, int zoomLevel, int tileX, int tileY) {
    super(BingClient.getBoundingBox(zoomLevel, tileX, tileY), 256, 256);
    this.layer = layer;
    this.quadKey = BingClient.getQuadKey(zoomLevel, tileX, tileY);
  }

  public String getQuadKey() {
    return quadKey;
  }

  @Override
  public Image loadImage() {
    final BingClient client = layer.getClient();
    ImagerySet imagerySet = layer.getImagerySet();
    MapLayer mapLayer = layer.getMapLayer();
    Image image = client.getMapImage(imagerySet, mapLayer, quadKey);
    return image;
  }

  @Override
  public int hashCode() {
    return quadKey.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BingMapTile) {
      BingMapTile tile = (BingMapTile)obj;
      if (tile.layer == layer) {
        if (tile.quadKey.equals(quadKey)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return layer + " " + quadKey;
  }
}
