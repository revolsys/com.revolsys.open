package com.revolsys.swing.map.layer.raster;

import org.springframework.core.io.Resource;

public class PngImageFactory extends AbstractGeoReferencedImageFactory {

  public PngImageFactory() {
    super("PNG");
    addMediaTypeAndFileExtension("image/png", "png");
   }

  @Override
  public GeoReferencedImage loadImage(Resource resource) {
    return new GeoReferencedImage(resource);
  }

}
