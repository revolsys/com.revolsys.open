package com.revolsys.swing.map.layer.raster;

import java.awt.Graphics2D;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayerRenderer;

public class RasterLayerRenderer extends AbstractLayerRenderer<RasterLayer> {

  public RasterLayerRenderer(final RasterLayer layer) {
    super("raster", layer);
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final RasterLayer layer) {

  }
}
