package com.revolsys.swing.map.layer.raster;

import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;

public abstract class AbstractTiledImageLayer<T extends GeoreferencedImageMapTile>
  extends AbstractTiledLayer<GeoreferencedImage, T> {

  public AbstractTiledImageLayer(final String type) {
    super(type);
  }

  @Override
  protected TiledGeoreferencedImageLayerRenderer<T> newRenderer() {
    return new TiledGeoreferencedImageLayerRenderer<>(this);
  }

}
