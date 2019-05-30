package com.revolsys.swing.map.layer.tile;

import java.util.Arrays;
import java.util.List;

import org.jeometry.common.logging.Logs;

import com.revolsys.collection.map.MapEx;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.BaseMapLayer;

public abstract class AbstractTiledLayer<D, T extends AbstractMapTile<D>> extends AbstractLayer
  implements BaseMapLayer {
  private boolean hasError = false;

  public AbstractTiledLayer(final String type) {
    super(type);
    setReadOnly(true);
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(newRenderer());
  }

  public abstract List<T> getOverlappingMapTiles(final Viewport2D view);

  public abstract double getResolution(final Viewport2D view);

  public boolean isHasError() {
    return this.hasError;
  }

  protected abstract AbstractTiledLayerRenderer<D, T> newRenderer();

  @Override
  protected void refreshDo() {
    this.hasError = false;
    super.refreshDo();
    final AbstractTiledLayerRenderer<D, T> renderer = getRenderer();
    renderer.clearCachedTiles();
  }

  public void setError(final Throwable e) {
    if (!this.hasError) {
      this.hasError = true;
      Logs.error(this, "Unable to get map tiles", e);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    map.keySet().removeAll(Arrays.asList("readOnly", "querySupported", "selectSupported"));
    return map;
  }
}
