package com.revolsys.format.tiff;

import org.springframework.core.io.Resource;

import com.revolsys.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.raster.GeoReferencedImage;

public class TiffImageFactory extends AbstractGeoReferencedImageFactory {

  public TiffImageFactory() {
    super("TIFF/GeoTIFF");
    addMediaTypeAndFileExtension("image/tiff", "tif");
    addMediaTypeAndFileExtension("image/tiff", "tiff");
  }

  @Override
  public GeoReferencedImage loadImage(final Resource resource) {
    return new TiffImage(resource);
  }

}
