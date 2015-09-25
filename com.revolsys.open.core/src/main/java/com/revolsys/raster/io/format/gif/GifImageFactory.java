package com.revolsys.raster.io.format.gif;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageFactory;
import com.revolsys.spring.resource.Resource;

public class GifImageFactory extends AbstractIoFactory implements GeoreferencedImageFactory {

  public GifImageFactory() {
    super("GIF");
    addMediaTypeAndFileExtension("image/gif", "gif");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new GifImage(resource);
  }

}
