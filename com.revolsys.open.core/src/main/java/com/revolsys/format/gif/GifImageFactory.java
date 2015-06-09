package com.revolsys.format.gif;

import org.springframework.core.io.Resource;

import com.revolsys.raster.AbstractGeoreferencedImageFactory;
import com.revolsys.raster.GeoreferencedImage;

public class GifImageFactory extends AbstractGeoreferencedImageFactory {

  public GifImageFactory() {
    super("GIF");
    addMediaTypeAndFileExtension("image/gif", "gif");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new GifImage(resource);
  }

}
