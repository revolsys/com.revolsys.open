package com.revolsys.format.png;

import org.springframework.core.io.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageFactory;

public class PngImageFactory extends AbstractIoFactory implements GeoreferencedImageFactory {

  public PngImageFactory() {
    super("PNG");
    addMediaTypeAndFileExtension("image/png", "png");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new PngImage(resource);
  }

}
