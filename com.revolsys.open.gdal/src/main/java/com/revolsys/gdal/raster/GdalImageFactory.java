package com.revolsys.gdal.raster;

import org.springframework.core.io.Resource;

import com.revolsys.gdal.Gdal;
import com.revolsys.raster.AbstractGeoreferencedImageFactory;
import com.revolsys.raster.GeoreferencedImage;

public class GdalImageFactory extends AbstractGeoreferencedImageFactory {

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
  public GeoreferencedImage loadImage(final Resource resource) {
    return new GdalImage(this, resource);
  }

  @Override
  public String toString() {
    return getName();
  }
}
