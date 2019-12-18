package com.revolsys.swing.map.layer.tile;

import org.jeometry.common.logging.Logs;

import com.revolsys.util.AbstractMapTile;
import com.revolsys.util.Cancellable;

public class TileLoadTask<D, T extends AbstractMapTile<D>> implements Runnable {
  private final T mapTile;

  private final AbstractTiledLayerRenderer<D, T> renderer;

  private final Cancellable cancellable;

  public TileLoadTask(final AbstractTiledLayerRenderer<D, T> renderer,
    final Cancellable cancellable, final T mapTile) {
    this.renderer = renderer;
    this.cancellable = cancellable;
    this.mapTile = mapTile;
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
        this.mapTile.loadData();
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

  @Override
  public String toString() {
    return this.mapTile.toString();
  }
}
