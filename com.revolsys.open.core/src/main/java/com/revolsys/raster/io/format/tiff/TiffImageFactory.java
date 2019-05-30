package com.revolsys.raster.io.format.tiff;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.spring.resource.Resource;

public class TiffImageFactory extends AbstractIoFactory implements GeoreferencedImageReadFactory {

  public TiffImageFactory() {
    super("TIFF/GeoTIFF");
    addMediaTypeAndFileExtension("image/tiff", "tif");
    addMediaTypeAndFileExtension("image/tiff", "tiff");
  }

  @Override
  public GeoreferencedImage readGeoreferencedImage(final Resource resource) {
    return new TiffImage(resource);
  }

}
