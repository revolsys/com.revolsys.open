package com.revolsys.format.tiff;

import org.springframework.core.io.Resource;

import com.revolsys.raster.AbstractGeoreferencedImageFactory;
import com.revolsys.raster.GeoreferencedImage;

public class TiffImageFactory extends AbstractGeoreferencedImageFactory {

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
