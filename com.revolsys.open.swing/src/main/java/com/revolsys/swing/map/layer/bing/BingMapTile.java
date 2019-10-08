package com.revolsys.swing.map.layer.bing;

import java.awt.image.BufferedImage;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.raster.GeoreferencedImageMapTile;

public class BingMapTile extends GeoreferencedImageMapTile {

  private final BingTiledLayer layer;

  private final String quadKey;

  public BingMapTile(final BingTiledLayer layer, final BoundingBox boundingBox, final int width,
    final int height) {
    super(boundingBox, width, height);
    this.layer = layer;
    this.quadKey = "";
  }

  public BingMapTile(final BingTiledLayer layer, final BoundingBox boundingBox,
    final String quadKey, final double resolution) {
    super(boundingBox, 256, 256);
    this.layer = layer;
    this.quadKey = quadKey;
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

  public BingTiledLayer getLayer() {
    return this.layer;
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
    final BingTiledLayer layer = this.layer;
    try {
      final BingClient client = layer.getClient();
      final ImagerySet imagerySet = layer.getImagerySet();
      final MapLayer mapLayer = layer.getMapLayer();
      final BufferedImage image = client.getMapImage(imagerySet, mapLayer, this.quadKey);
      return image;
    } catch (final Throwable t) {
      layer.setError(t);
      return null;
    }
  }

  @Override
  public String toString() {
    return this.layer + " " + this.quadKey;
  }
}
