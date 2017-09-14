package com.revolsys.swing.map.layer.tile;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.logging.Logs;
import com.revolsys.util.Cancellable;

public class TileLoadTask<D, T extends AbstractMapTile<D>> implements Runnable {
  private final GeometryFactory geometryFactory;

  private final T mapTile;

  private final AbstractTiledLayerRenderer<D, T> renderer;

  private final Cancellable cancellable;

  public TileLoadTask(final AbstractTiledLayerRenderer<D, T> renderer,
    final Cancellable cancellable, final GeometryFactory geometryFactory, final T mapTile) {
    this.renderer = renderer;
    this.cancellable = cancellable;
    this.geometryFactory = geometryFactory;
    this.mapTile = mapTile;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public T getMapTile() {
    return this.mapTile;
  }

  public AbstractTiledLayerRenderer<D, T> getRenderer() {
    return this.renderer;
  }

  @Override
  public void run() {
    try {
      if (!this.cancellable.isCancelled()) {
        this.mapTile.loadData(this.geometryFactory);
      }
      if (!this.cancellable.isCancelled()) {
        this.renderer.setLoaded(this);
      }
    } catch (final RuntimeException e) {
      if (!this.cancellable.isCancelled()) {
        Logs.error(this, "Unable to load " + this.mapTile, e);
      }
    }
  }

}
