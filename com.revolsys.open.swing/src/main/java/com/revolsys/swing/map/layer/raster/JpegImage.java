package com.revolsys.swing.map.layer.raster;

import org.springframework.core.io.Resource;

public class JpegImage extends GeoReferencedImage {

  public JpegImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  protected void loadImageMetaData() {
    final Resource resource = getImageResource();
    loadProjectionFile(resource);
    loadWorldFile(resource, "jgw");
  }
}
