package com.revolsys.raster.io.format.tiff;

public interface GeoTiffConstants {

  /** Projection Coordinate System */
  int ModelTypeProjected = 1;

  /** Geographic latitude-longitude System */
  int ModelTypeGeographic = 2;

  /** Geocentric (X,Y,Z) Coordinate System */
  int ModelTypeGeocentric = 3;

  int GTModelTypeGeoKey = 1024;

  int GTRasterTypeGeoKey = 1025;

  int GTCitationGeoKey = 1026;

  // 6.2.2 Geographic CS Parameter Keys
  int GeographicTypeGeoKey = 2048;

  int GeogCitationGeoKey = 2049;

  int GeogGeodeticDatumGeoKey = 2050;

  int GeogPrimeMeridianGeoKey = 2051;

  int GeogLinearUnitsGeoKey = 2052;

  int GeogLinearUnitSizeGeoKey = 2053;

  int GeogAngularUnitsGeoKey = 2054;

  int GeogAngularUnitSizeGeoKey = 2055;

  int GeogEllipsoidGeoKey = 2056;

  int GeogSemiMajorAxisGeoKey = 2057;

  int GeogSemiMinorAxisGeoKey = 2058;

  int GeogInvFlatteningGeoKey = 2059;

  int GeogAzimuthUnitsGeoKey = 2060;

  int GeogPrimeMeridianLongGeoKey = 2061;

  // 6.2.3 Projected CS Parameter Keys

  int ProjectedCSTypeGeoKey = 3072;

  int PCSCitationGeoKey = 3073;

  int ProjectionGeoKey = 3074;

  int ProjCoordTransGeoKey = 3075;

  int ProjLinearUnitsGeoKey = 3076;

  int ProjLinearUnitSizeGeoKey = 3077;

  // 6.2.4 Vertical CS Keys
  int VerticalCSTypeGeoKey = 4096;

  int VerticalCitationGeoKey = 4097;

  int VerticalDatumGeoKey = 4098;

  int VerticalUnitsGeoKey = 4099;

  int RasterPixelIsArea = 1;

  int RasterPixelIsPoint = 2;

}
