package com.revolsys.gdal.raster;

import org.springframework.core.io.Resource;

import com.revolsys.gdal.Gdal;
import com.revolsys.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.raster.GeoReferencedImage;

public class GdalImageFactory extends AbstractGeoReferencedImageFactory {

  private final String driverName;

  public GdalImageFactory(final String driverName, final String formatName,
    final String fileExtension, final String mimeType) {
    super(formatName);
    addMediaTypeAndFileExtension(mimeType, fileExtension);
    this.driverName = driverName;
  }

  @Override
  public boolean isAvailable() {
    return Gdal.isDriverAvailable(this.driverName);
  }

  @Override
  public GeoReferencedImage loadImage(final Resource resource) {
    return new GdalImage(this, resource);
  }

  @Override
  public String toString() {
    return getName();
  }
}
