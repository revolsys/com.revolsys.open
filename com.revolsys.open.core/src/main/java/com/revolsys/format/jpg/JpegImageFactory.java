package com.revolsys.format.jpg;

import org.springframework.core.io.Resource;

import com.revolsys.raster.AbstractGeoreferencedImageFactory;
import com.revolsys.raster.GeoreferencedImage;

public class JpegImageFactory extends AbstractGeoreferencedImageFactory {

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
