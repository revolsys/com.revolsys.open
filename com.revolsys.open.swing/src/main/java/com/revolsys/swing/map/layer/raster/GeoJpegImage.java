package com.revolsys.swing.map.layer.raster;

import javax.media.jai.RenderedOp;

import org.springframework.core.io.Resource;

public class GeoJpegImage extends GeoReferencedImage {

  public GeoJpegImage(final Resource imageResource) {
    super(imageResource);
  }

  @Override
  protected void loadImageMetaData(final Resource resource,
    final RenderedOp image) {
    loadProjectionFile(resource);
    loadWorldFile(resource, "jgw");
  }
}
