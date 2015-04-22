package com.revolsys.format.gif;

import org.springframework.core.io.Resource;

import com.revolsys.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.raster.GeoReferencedImage;

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
