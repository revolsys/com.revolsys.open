package com.revolsys.swing.map.layer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.map.Viewport2D;

public abstract class AbstractTiledImageLayer extends AbstractLayer {

  private boolean hasError = false;

  public AbstractTiledImageLayer() {
    this(null, true, false, false);
  }

  public AbstractTiledImageLayer(final String name, final boolean readOnly,
    final boolean selectSupported, final boolean querySupported) {
    super(name);
    setReadOnly(readOnly);
    setSelectSupported(selectSupported);
    setQuerySupported(querySupported);
    setRenderer(new TiledImageLayerRenderer(this));
  }

  public TileLoaderProcess createTileLoaderProcess() {
    return new TileLoaderProcess(this);
  }

  public abstract List<MapTile> getOverlappingMapTiles(final Viewport2D viewport);

  public abstract double getResolution(final Viewport2D viewport);

  public boolean isHasError() {
    return this.hasError;
  }

  @Override
  public void refresh() {
    this.hasError = false;
    super.refresh();
    firePropertyChange("refresh", false, true);
  }

  public void setError(final Throwable e) {
    if (!this.hasError) {
      this.hasError = true;
      LoggerFactory.getLogger(getClass()).error("Unable to get map tiles", e);
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    map.keySet().removeAll(
      Arrays.asList("readOnly", "querySupported", "selectSupported"));
    return map;
  }
}
