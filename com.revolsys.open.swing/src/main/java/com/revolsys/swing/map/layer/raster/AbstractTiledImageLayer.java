package com.revolsys.swing.map.layer.raster;

import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.swing.map.layer.BaseMapLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;

public abstract class AbstractTiledImageLayer<T extends GeoreferencedImageMapTile>
  extends AbstractTiledLayer<GeoreferencedImage, T> implements BaseMapLayer {

  public AbstractTiledImageLayer(final String type) {
    super(type);
  }

  @Override
  protected TiledImageLayerRenderer<T> newRenderer() {
    return new TiledImageLayerRenderer<>(this);
  }

}
