package com.revolsys.swing.map.layer;

import java.util.List;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.Viewport2D;
public abstract class AbstractTiledImageLayer extends AbstractLayer {

  public AbstractTiledImageLayer() {
  }

  public AbstractTiledImageLayer(final String name, final boolean readOnly,
    final boolean selectSupported, final boolean querySupported) {
    super(name);
    setReadOnly(readOnly);
    setSelectSupported(selectSupported);
    setQuerySupported(querySupported);
  }

  public abstract List<MapTile> getOverlappingEnvelopes(
    final Viewport2D viewport);

  public abstract TileLoaderProcess getTileLoaderProcess();

}
