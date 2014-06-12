package com.revolsys.raster;

import org.springframework.core.io.Resource;

public class PngImageFactory extends AbstractGeoReferencedImageFactory {

  public PngImageFactory() {
    super("PNG");
    addMediaTypeAndFileExtension("image/png", "png");
  }

  @Override
  public GeoReferencedImage loadImage(final Resource resource) {
    return new PngImage(resource);
  }

}
