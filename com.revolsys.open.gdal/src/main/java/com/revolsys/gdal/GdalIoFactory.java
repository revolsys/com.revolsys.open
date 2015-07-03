package com.revolsys.gdal;

import com.revolsys.io.AbstractIoFactory;

public class GdalIoFactory extends AbstractIoFactory {
  static {
    Gdal.init();
  }

  public GdalIoFactory() {
    super("GDAL");
  }

  @Override
  public boolean isAvailable() {
    return false;
  }

}
