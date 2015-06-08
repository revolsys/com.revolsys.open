package com.revolsys.swing.map.layer.bing;

import java.awt.image.BufferedImage;

import com.revolsys.swing.map.layer.MapTile;

public class BingMapTile extends MapTile {

  private final String quadKey;

  private final BingLayer layer;

  public BingMapTile(final BingLayer layer, final int zoomLevel, final double resolution,
    final int tileX, final int tileY) {
    super(layer.getClient().getBoundingBox(zoomLevel, tileX, tileY), 256, 256, resolution);
    this.layer = layer;
    this.quadKey = layer.getClient().getQuadKey(zoomLevel, tileX, tileY);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof BingMapTile) {
      final BingMapTile tile = (BingMapTile)obj;
      if (tile.layer == this.layer) {
        if (tile.quadKey.equals(this.quadKey)) {
          return true;
        }
      }
    }
    return false;
  }

  public String getQuadKey() {
    return this.quadKey;
  }

  @Override
  public int hashCode() {
    return this.quadKey.hashCode();
  }

  @Override
  public BufferedImage loadBuffferedImage() {
    try {
      final BingClient client = this.layer.getClient();
      final ImagerySet imagerySet = this.layer.getImagerySetEnum();
      final MapLayer mapLayer = this.layer.getMapLayerEnum();
      final BufferedImage image = client.getMapImage(imagerySet, mapLayer, this.quadKey);
      return image;
    } catch (final Throwable t) {
      this.layer.setError(t);
      return null;
    }
  }

  @Override
  public String toString() {
    return this.layer + " " + this.quadKey;
  }
}
