package com.revolsys.format.usgsdem;

import org.springframework.core.io.Resource;

import com.revolsys.raster.AbstractGeoreferencedImageFactory;
import com.revolsys.raster.GeoreferencedImage;

public class UsgsDemImageFactory extends AbstractGeoreferencedImageFactory {

  public UsgsDemImageFactory() {
    super("USGS DEM");
    addMediaTypeAndFileExtension("image/x-dem", "dem");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new UsgsDemGeoreferencedImage(resource);
  }

}
