package com.revolsys.swing.map.layer.arcgisrest;

import java.awt.Image;

import com.revolsys.io.esri.map.rest.MapServer;
import com.revolsys.swing.map.layer.MapTile;

public class ArcGisServerRestMapTile extends MapTile {

  private MapServer mapServer;

  private int zoomLevel;

  private int tileX;

  private int tileY;

  public ArcGisServerRestMapTile(MapServer mapServer, int zoomLevel, int tileX,
    int tileY) {
    super(mapServer.getBoundingBox(zoomLevel, tileX, tileY),
      mapServer.getTileInfo().getWidth(), mapServer.getTileInfo().getHeight());
    this.mapServer = mapServer;
    this.zoomLevel = zoomLevel;
    this.tileX = tileX;
    this.tileY = tileY;
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

  public MapServer getMapServer() {
    return mapServer;
  }

  public Image loadImage() {
    Image image = mapServer.getTileImage(zoomLevel, tileX, tileY);
    return image;
  }
}
