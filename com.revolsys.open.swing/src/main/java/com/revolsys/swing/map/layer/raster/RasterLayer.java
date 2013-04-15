package com.revolsys.swing.map.layer.raster;

import com.revolsys.swing.map.layer.AbstractLayer;

public class RasterLayer extends AbstractLayer {
  public RasterLayer() {
    setRenderer(new RasterLayerRenderer(this));
  }
}
