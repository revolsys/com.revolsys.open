package com.revolsys.swing.map.layer;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.swing.map.layer.raster.GeoReferencedImage;

public class MapTile extends GeoReferencedImage {
  public MapTile(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight) {
    super(boundingBox, imageWidth, imageHeight);
  }
}
