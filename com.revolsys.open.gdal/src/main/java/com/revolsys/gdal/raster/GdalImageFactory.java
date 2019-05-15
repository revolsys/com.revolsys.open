package com.revolsys.gdal.raster;

import com.revolsys.gdal.Gdal;
import com.revolsys.io.AbstractIoFactory;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.raster.GeoreferencedImageReadFactory;
import com.revolsys.spring.resource.Resource;

public class GdalImageFactory extends AbstractIoFactory implements GeoreferencedImageReadFactory {

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
  public GeoreferencedImage readGeoreferencedImage(final Resource resource) {
    return new GdalImage(this, resource);
  }

  @Override
  public String toString() {
    return getName();
  }
}
