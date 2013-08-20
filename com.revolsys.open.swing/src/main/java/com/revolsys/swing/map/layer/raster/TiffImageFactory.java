package com.revolsys.swing.map.layer.raster;

import org.springframework.core.io.Resource;

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
