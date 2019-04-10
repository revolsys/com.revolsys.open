package com.revolsys.swing.map.layer.raster;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.logging.Logs;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.util.Cancellable;

public class TileLoadTask implements Runnable {
  private final GeometryFactory geometryFactory;

  private final MapTile mapTile;

  private final TiledImageLayerRenderer renderer;

  private final Cancellable cancellable;

  private final double resolution;

  public TileLoadTask(final TiledImageLayerRenderer renderer, final Cancellable cancellable,
    final GeometryFactory geometryFactory, final MapTile mapTile, final double resolution) {
    this.renderer = renderer;
    this.cancellable = cancellable;
    this.geometryFactory = geometryFactory;
    this.mapTile = mapTile;
    this.resolution = resolution;
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
      if (!this.cancellable.isCancelled()) {
        this.mapTile.loadImage(this.geometryFactory, this.resolution);
      }
      if (!this.cancellable.isCancelled()) {
        this.renderer.setLoaded(this);
      }
    } catch (final Throwable e) {
      if (!this.cancellable.isCancelled()) {
        Logs.error(this, "Unable to load " + this.mapTile, e);
      }
    }
  }

}
