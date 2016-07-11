package com.revolsys.swing.map.layer.raster;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.logging.Logs;
import com.revolsys.swing.map.layer.MapTile;

public class TileLoadTask implements Runnable {
  private final GeometryFactory geometryFactory;

  private final MapTile mapTile;

  private final TiledImageLayerRenderer renderer;

  public TileLoadTask(final TiledImageLayerRenderer renderer, final GeometryFactory geometryFactory,
    final MapTile mapTile) {
    this.renderer = renderer;
    this.geometryFactory = geometryFactory;
    this.mapTile = mapTile;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public MapTile getMapTile() {
    return this.mapTile;
  }

  public TiledImageLayerRenderer getRenderer() {
    return this.renderer;
  }

  @Override
  public void run() {
    try {
      this.mapTile.loadImage(this.geometryFactory);
      this.renderer.setLoaded(this);
    } catch (final Throwable e) {
      Logs.error(this, "Unable to load " + this.mapTile, e);
    }
  }

}
