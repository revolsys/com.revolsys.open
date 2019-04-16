package com.revolsys.swing.map.layer.tile;

import org.jeometry.common.logging.Logs;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.util.Cancellable;

public class TileLoadTask<D, T extends AbstractMapTile<D>> implements Runnable {
  private final GeometryFactory geometryFactory;

  private final T mapTile;

  private final AbstractTiledLayerRenderer<D, T> renderer;

  private final Cancellable cancellable;

  private final double layerResolution;

  private final double viewResolution;

  public TileLoadTask(final AbstractTiledLayerRenderer<D, T> renderer,
    final Cancellable cancellable, final GeometryFactory geometryFactory, final T mapTile,
    final double layerResolution, final double viewResolution) {
    this.renderer = renderer;
    this.cancellable = cancellable;
    this.geometryFactory = geometryFactory;
    this.mapTile = mapTile;
    this.layerResolution = layerResolution;
    this.viewResolution = viewResolution;
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
        this.mapTile.loadData(this.geometryFactory, this.layerResolution, this.viewResolution);
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
