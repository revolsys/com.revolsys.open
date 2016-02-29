package com.revolsys.swing.map.layer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.raster.TiledImageLayerRenderer;
import com.revolsys.util.Exceptions;

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
      Exceptions.log(getClass(), "Unable to get map tiles", e);
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    map.keySet().removeAll(Arrays.asList("readOnly", "querySupported", "selectSupported"));
    return map;
  }
}
