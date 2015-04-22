package com.revolsys.format.jpg;

import org.springframework.core.io.Resource;

import com.revolsys.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.raster.GeoReferencedImage;

public class JpegImageFactory extends AbstractGeoReferencedImageFactory {

  public JpegImageFactory() {
    super("JPEG");
    addMediaTypeAndFileExtension("image/jpeg", "jpg");
    addMediaTypeAndFileExtension("image/jpeg", "jpeg");
  }

  @Override
  public GeoReferencedImage loadImage(final Resource resource) {
    return new JpegImage(resource);
  }

}
