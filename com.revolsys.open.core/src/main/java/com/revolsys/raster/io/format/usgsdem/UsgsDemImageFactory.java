package com.revolsys.raster.io.format.usgsdem;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageFactory;
import com.revolsys.spring.resource.Resource;

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
