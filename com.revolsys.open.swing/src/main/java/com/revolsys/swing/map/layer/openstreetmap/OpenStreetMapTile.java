package com.revolsys.swing.map.layer.openstreetmap;

import java.awt.image.BufferedImage;

import com.revolsys.swing.map.layer.MapTile;

public class OpenStreetMapTile extends MapTile {

  private final OpenStreetMapLayer layer;

  private final int zoomLevel;

  private final int tileX;

  private final int tileY;

  public OpenStreetMapTile(final OpenStreetMapLayer layer, final int zoomLevel,
    final double resolution, final int tileX, final int tileY) {
    super(layer.getClient().getBoundingBox(zoomLevel, tileX, tileY), 256, 256, resolution);
    this.layer = layer;
    this.zoomLevel = zoomLevel;
    this.tileX = tileX;
    this.tileY = tileY;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof OpenStreetMapTile) {
      final OpenStreetMapTile tile = (OpenStreetMapTile)obj;
      if (tile.layer == this.layer) {
        if (tile.zoomLevel == this.zoomLevel) {
          if (tile.tileX == this.tileX) {
            if (tile.tileY == this.tileY) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public int getTileX() {
    return this.tileX;
  }

  public int getTileY() {
    return this.tileY;
  }

  public int getZoomLevel() {
    return this.zoomLevel;
  }

  @Override
  public int hashCode() {
    return this.zoomLevel + this.tileX + this.tileY;
  }

  @Override
  public BufferedImage loadBuffferedImage() {
    try {
      final OpenStreetMapClient client = this.layer.getClient();
      final BufferedImage image = client.getMapImage(this.zoomLevel, this.tileX, this.tileY);
      return image;
    } catch (final Throwable e) {
      return null;
    }
  }

  @Override
  public String toString() {
    return this.layer + " " + this.zoomLevel + "/" + this.tileX + "/" + this.tileY;
  }
}
