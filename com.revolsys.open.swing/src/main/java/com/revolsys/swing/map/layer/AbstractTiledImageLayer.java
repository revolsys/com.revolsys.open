package com.revolsys.swing.map.layer;

import java.util.Arrays;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.logging.Logs;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.TiledImageLayerRenderer;

public abstract class AbstractTiledImageLayer extends AbstractLayer implements BaseMapLayer {
  private boolean hasError = false;

  public AbstractTiledImageLayer(final String type) {
    super(type);
    setReadOnly(true);
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(new TiledImageLayerRenderer(this));
  }

  public abstract List<MapTile> getOverlappingMapTiles(final Viewport2D viewport);

  public abstract double getResolution(final Viewport2D viewport);

  public boolean isHasError() {
    return this.hasError;
  }

  @Override
  protected void refreshDo() {
    this.hasError = false;
    super.refreshDo();
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
