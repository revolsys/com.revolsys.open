package com.revolsys.swing.map.layer.tile;

import java.util.Arrays;
import java.util.List;

import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.BaseMapLayer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.AbstractMapTile;

public abstract class AbstractTiledLayer<D, T extends AbstractMapTile<D>> extends AbstractLayer
  implements BaseMapLayer {

  public AbstractTiledLayer(final String type) {
    super(type);
    setReadOnly(true);
    setSelectSupported(false);
    setQuerySupported(false);
    setRenderer(newRenderer());
  }

  public abstract List<T> getOverlappingMapTiles(AbstractTiledLayerRenderer<?, ?> renderer,
    final ViewRenderer view);

  public abstract double getResolution(final ViewRenderer view);

  protected abstract AbstractTiledLayerRenderer<D, T> newRenderer();

  @Override
  protected void refreshDo() {
    super.refreshDo();
    final AbstractTiledLayerRenderer<D, T> renderer = getRenderer();
    renderer.clearCachedTiles();
  }

  public void setError(final String message, final Throwable e) {
    final AbstractTiledLayerRenderer<D, T> renderer = getRenderer();
    renderer.setError(message, e);
  }

  public void setError(final Throwable e) {
    setError("Error loading '" + getPath() + "', move the map or Refresh the layer to try again",
      e);
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    map.keySet().removeAll(Arrays.asList("readOnly", "querySupported", "selectSupported"));
    return map;
  }
}
