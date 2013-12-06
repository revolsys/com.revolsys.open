package com.revolsys.swing.map.layer.raster;

import org.springframework.core.io.Resource;

public class GifImageFactory extends AbstractGeoReferencedImageFactory {

  public GifImageFactory() {
    super("GIF");
    addMediaTypeAndFileExtension("image/gif", "gif");
  }

  @Override
  public GeoReferencedImage loadImage(final Resource resource) {
    return new GifImage(resource);
  }

}
