package com.revolsys.swing.map.layer.bing;

import com.revolsys.gis.bing.BingClient;
import com.revolsys.swing.map.layer.MapTile;

public class BingMapTile extends MapTile {

  private String quadKey;

  public BingMapTile(int zoomLevel, int tileX, int tileY) {
    super(BingClient.getBoundingBox(zoomLevel, tileX, tileY));
    this.quadKey = BingClient.getQuadKey(zoomLevel, tileX, tileY);
  }

  public String getQuadKey() {
    return quadKey;
  }

}
