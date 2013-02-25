package com.revolsys.swing.map.layer.bing;

import java.awt.Image;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.bing.BingClient;
import com.revolsys.gis.bing.BingClient.ImagerySet;
import com.revolsys.gis.bing.BingClient.MapLayer;
import com.revolsys.swing.map.layer.TileLoaderProcess;

public class BingTileImageLoaderProcess extends TileLoaderProcess {

  private final BingLayer layer;

  public BingTileImageLoaderProcess(final BingLayer layer) {
    this.layer = layer;
  }

  @Override
  protected Image doInBackground() throws Exception {
    return getImage();
  }

  @Override
  public BingMapTile getMapTile() {
    return (BingMapTile)super.getMapTile();
  }

  public Image getImage() throws IOException {
    try {
      final BingClient client = layer.getClient();
      String quadKey = getMapTile().getQuadKey();
      ImagerySet imagerySet = layer.getImagerySet();
      MapLayer mapLayer = layer.getMapLayer();
      Image image = client.getMapImage(imagerySet, mapLayer, quadKey);
      return image;
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).error("Unable to load Bing tile", e);
      return null;
    }
  }

  @Override
  public String toString() {
    return "Loading Bing Map";
  }
}
