package com.revolsys.format.usgsdem;

import org.springframework.core.io.Resource;

import com.revolsys.format.tiff.TiffImage;
import com.revolsys.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.raster.GeoReferencedImage;

public class UsgsDemImageFactory extends AbstractGeoReferencedImageFactory {

  public UsgsDemImageFactory() {
    super("USGS DEM");
    addMediaTypeAndFileExtension("image/x-dem", "dem");
  }

  @Override
  public GeoReferencedImage loadImage(final Resource resource) {
    return new UsgsDemGeoReferencedImage(resource);
  }

}
