package com.revolsys.swing.map.layer.arcgisrest;

import java.awt.image.BufferedImage;

import com.revolsys.record.io.format.esri.rest.map.ArcGisRestMapServer;
import com.revolsys.swing.map.layer.MapTile;

public class ArcGisRestServerTileCacheMapTile extends MapTile {
  private final ArcGisRestServerTileCacheLayer layer;

  private final ArcGisRestMapServer mapServer;

  private final int tileX;

  private final int tileY;

  private final int zoomLevel;

  public ArcGisRestServerTileCacheMapTile(final ArcGisRestServerTileCacheLayer layer,
    final ArcGisRestMapServer mapServer, final int zoomLevel, final double resolution,
    final int tileX, final int tileY) {

    super(mapServer.getBoundingBox(zoomLevel, tileX, tileY), mapServer.getTileInfo().getWidth(),
      mapServer.getTileInfo().getHeight(), resolution);
    this.layer = layer;
    this.mapServer = mapServer;
    this.zoomLevel = zoomLevel;
    this.tileX = tileX;
    this.tileY = tileY;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ArcGisRestServerTileCacheMapTile) {
      final ArcGisRestServerTileCacheMapTile tile = (ArcGisRestServerTileCacheMapTile)obj;
      if (tile.getMapServer() == getMapServer()) {
        if (tile.getZoomLevel() == getZoomLevel()) {
          if (tile.getTileX() == getTileX()) {
            if (tile.getTileY() == getTileY()) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  public ArcGisRestMapServer getMapServer() {
    return this.mapServer;
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
    return this.zoomLevel << 24 & this.tileX << 16 & this.tileY << 8;
  }

  @Override
  protected BufferedImage loadBuffferedImage() {
    try {
      return this.mapServer.getTileImage(this.zoomLevel, this.tileX, this.tileY);
    } catch (final Throwable t) {
      this.layer.setError(t);
      return null;
    }
  }

  @Override
  public String toString() {
    return this.mapServer.getMapName() + " " + this.zoomLevel + "/" + this.tileX + "/" + this.tileY;
  }
}
