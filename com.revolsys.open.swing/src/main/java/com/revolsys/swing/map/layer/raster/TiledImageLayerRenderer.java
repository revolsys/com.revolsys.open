package com.revolsys.swing.map.layer.raster;

import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;

import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayerRenderer;
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
  protected void renderTile(final Viewport2D viewport, final Cancellable cancellable,
    final T tile) {
    final Graphics2D graphics = viewport.getGraphics();
    if (graphics != null) {
      final GeometryFactory viewportGeometryFactory = viewport.getGeometryFactory();
      final GeoreferencedImage image = tile.getImage(viewportGeometryFactory);
      GeoreferencedImageLayerRenderer.render(viewport, graphics, image, false);
    }
  }

  @Override
  public MapEx toMap() {
    return MapEx.EMPTY;
  }
}
