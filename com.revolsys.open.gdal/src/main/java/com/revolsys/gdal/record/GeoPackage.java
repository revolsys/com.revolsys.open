package com.revolsys.gdal.record;

public class GeoPackage extends OgrRecordStoreFactory {
  public GeoPackage() {
    super("GeoPackage", "GPKG", "application/x-gpkg", "gpkg");
  }
}
