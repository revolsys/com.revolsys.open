package com.revolsys.format.tiff;

import com.revolsys.spring.resource.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageFactory;

public class TiffImageFactory extends AbstractIoFactory implements GeoreferencedImageFactory {

  public TiffImageFactory() {
    super("TIFF/GeoTIFF");
    addMediaTypeAndFileExtension("image/tiff", "tif");
    addMediaTypeAndFileExtension("image/tiff", "tiff");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new TiffImage(resource);
  }

}
