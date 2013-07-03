package com.revolsys.swing.map.layer.arcgisrest;

import java.awt.image.BufferedImage;

import com.revolsys.io.esri.map.rest.MapServer;
import com.revolsys.swing.map.layer.MapTile;

public class ArcGisServerRestMapTile extends MapTile {

  private final MapServer mapServer;

  private final int zoomLevel;

  private final int tileX;

  private final int tileY;

  private final ArcGisServerRestLayer layer;

  public ArcGisServerRestMapTile(final ArcGisServerRestLayer layer,
    final int zoomLevel, final double resolution, final int tileX,
    final int tileY) {

    super(layer.getMapServer().getBoundingBox(zoomLevel, tileX, tileY),
      layer.getMapServer().getTileInfo().getWidth(), layer.getMapServer()
        .getTileInfo()
        .getHeight(), resolution);
    this.layer = layer;
    this.mapServer = layer.getMapServer();
    this.zoomLevel = zoomLevel;
    this.tileX = tileX;
    this.tileY = tileY;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ArcGisServerRestMapTile) {
      final ArcGisServerRestMapTile tile = (ArcGisServerRestMapTile)obj;
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

  public MapServer getMapServer() {
    return mapServer;
  }

  public int getTileX() {
    return tileX;
  }

  public int getTileY() {
    return tileY;
  }

  public int getZoomLevel() {
    return zoomLevel;
  }

  @Override
  public int hashCode() {
    return zoomLevel << 24 & tileX << 16 & tileY << 8;
  }

  @Override
  protected BufferedImage loadBuffferedImage() {
    try {
      return mapServer.getTileImage(zoomLevel, tileX, tileY);
    } catch (final Throwable t) {
      layer.setError(t);
      return null;
    }
  }

  @Override
  public String toString() {
    return mapServer.getMapName() + " " + zoomLevel + "/" + tileX + "/" + tileY;
  }
}
