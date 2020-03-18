package com.revolsys.swing.map.layer.webmercatortilecache;

import java.awt.image.BufferedImage;

import com.revolsys.raster.GeoreferencedImageMapTile;

public class WebMercatorTileCacheMapTile extends GeoreferencedImageMapTile {
  private final WebMercatorTileCacheLayer layer;

  private final int tileX;

  private final int tileY;

  private final int zoomLevel;

  public WebMercatorTileCacheMapTile(final WebMercatorTileCacheLayer layer, final int zoomLevel,
    final double resolution, final int tileX, final int tileY) {
    super(layer.getClient().getBoundingBox(zoomLevel, tileX, tileY), 256, 256);
    this.layer = layer;
    this.zoomLevel = zoomLevel;
    this.tileX = tileX;
    this.tileY = tileY;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof WebMercatorTileCacheMapTile) {
      final WebMercatorTileCacheMapTile tile = (WebMercatorTileCacheMapTile)obj;
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
      final WebMercatorTileCacheClient client = this.layer.getClient();
      final BufferedImage image = client.getMapImage(this.zoomLevel, this.tileX, this.tileY);
      return image;
    } catch (final Throwable e) {
      this.layer.setError(e);
      return null;
    }
  }

  @Override
  public String toString() {
    return this.layer + " " + this.zoomLevel + "/" + this.tileX + "/" + this.tileY;
  }
}
