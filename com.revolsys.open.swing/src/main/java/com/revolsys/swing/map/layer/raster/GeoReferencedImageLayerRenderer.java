package com.revolsys.swing.map.layer.raster;

import java.awt.Graphics2D;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;
import com.revolsys.swing.map.layer.TiledImageLayerRenderer;

public class GeoReferencedImageLayerRenderer extends
  AbstractLayerRenderer<GeoReferencedImageLayer> {
  public GeoReferencedImageLayerRenderer(final GeoReferencedImageLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final GeoReferencedImageLayer layer) {
    if (layer.isVisible()) {
      GeoReferencedImage image = layer.getImage();
      TiledImageLayerRenderer.render(viewport, graphics, image);
    }
  }
}
