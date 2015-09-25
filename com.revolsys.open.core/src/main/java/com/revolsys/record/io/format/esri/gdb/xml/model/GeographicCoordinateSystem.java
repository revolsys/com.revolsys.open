package com.revolsys.record.io.format.esri.gdb.xml.model;

import com.revolsys.geometry.model.GeometryFactory;

public class GeographicCoordinateSystem extends SpatialReference {

  public GeographicCoordinateSystem() {
  }

  public GeographicCoordinateSystem(final GeometryFactory geometryFactory, final String wkt) {
    super(geometryFactory, wkt);
  }

}
