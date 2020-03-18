package com.revolsys.swing.map.layer.raster;

import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageMapTile;
import com.revolsys.swing.map.layer.BaseMapLayer;
import com.revolsys.swing.map.layer.tile.AbstractTiledLayer;

public abstract class AbstractTiledGeoreferencedImageLayer<T extends GeoreferencedImageMapTile>
  extends AbstractTiledLayer<GeoreferencedImage, T> implements BaseMapLayer {

  public AbstractTiledGeoreferencedImageLayer(final String type) {
    super(type);
  }

  @Override
  protected TiledGeoreferencedImageLayerRenderer<T> newRenderer() {
    return new TiledGeoreferencedImageLayerRenderer<>(this);
  }

}
