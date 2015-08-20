package com.revolsys.format.usgsdem;

import com.revolsys.spring.resource.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageFactory;

public class UsgsDemImageFactory extends AbstractIoFactory implements GeoreferencedImageFactory {

  public UsgsDemImageFactory() {
    super("USGS DEM");
    addMediaTypeAndFileExtension("image/x-dem", "dem");
  }

  @Override
  public GeoreferencedImage loadImage(final Resource resource) {
    return new UsgsDemGeoreferencedImage(resource);
  }

}
