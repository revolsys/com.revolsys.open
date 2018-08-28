package com.revolsys.swing.map.layer.raster;

import java.beans.PropertyChangeListener;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
import com.revolsys.swing.map.view.ViewRenderer;
import com.revolsys.util.Cancellable;

public class TiledImageLayerRenderer<T extends GeoreferencedImageMapTile>
  extends AbstractTiledLayerRenderer<GeoreferencedImage, T> implements PropertyChangeListener {

  public TiledImageLayerRenderer(final AbstractTiledImageLayer<T> layer) {
    super("tiledImage", layer);
  }

  public TiledImageLayerRenderer(final String type, final String name) {
    super(type, name);
  }

  @Override
  protected void renderTile(final ViewRenderer viewport, final Cancellable cancellable,
    final T tile) {
    final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
    final GeoreferencedImage image = tile.getImage(viewportGeometryFactory);
    if (image != null) {
      viewport.drawImage(image, false);
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
