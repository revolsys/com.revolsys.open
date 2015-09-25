package com.revolsys.raster.io.format.jpg;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageFactory;
import com.revolsys.spring.resource.Resource;

public class JpegImageFactory extends AbstractIoFactory implements GeoreferencedImageFactory {

  public JpegImageFactory() {
    super("JPEG");
    addMediaTypeAndFileExtension("image/jpeg", "jpg");
    addMediaTypeAndFileExtension("image/jpeg", "jpeg");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new JpegImage(resource);
  }

}
