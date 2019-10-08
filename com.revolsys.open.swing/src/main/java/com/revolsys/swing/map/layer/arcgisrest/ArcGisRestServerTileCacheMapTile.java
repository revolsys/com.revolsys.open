package com.revolsys.swing.map.layer.arcgisrest;

import java.awt.image.BufferedImage;

import com.revolsys.raster.GeoreferencedImageMapTile;
import com.revolsys.record.io.format.esri.rest.map.MapService;

public class ArcGisRestServerTileCacheMapTile extends GeoreferencedImageMapTile {
  private final ArcGisRestServerTileCacheLayer layer;

  private final MapService mapService;

  private final int tileX;

  private final int tileY;

  private final int zoomLevel;

  public ArcGisRestServerTileCacheMapTile(final ArcGisRestServerTileCacheLayer layer,
    final MapService mapService, final int zoomLevel, final double resolution, final int tileX,
    final int tileY) {
    super(mapService.getBoundingBox(zoomLevel, tileX, tileY), mapService.getTileInfo().getCols(),
      mapService.getTileInfo().getRows());
    this.layer = layer;
    this.mapService = mapService;
    this.zoomLevel = zoomLevel;
    this.tileX = tileX;
    this.tileY = tileY;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof ArcGisRestServerTileCacheMapTile) {
      final ArcGisRestServerTileCacheMapTile tile = (ArcGisRestServerTileCacheMapTile)obj;
      if (tile.getMapService() == getMapService()) {
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

  public MapService getMapService() {
    return this.mapService;
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
      return this.mapService.getTileImage(this.zoomLevel, this.tileX, this.tileY);
    } catch (final Throwable e) {
      this.layer.setError(e);
      return null;
    }
  }

  @Override
  public String toString() {
    return this.mapService.getMapName() + " " + this.zoomLevel + "/" + this.tileX + "/"
      + this.tileY;
  }
}
