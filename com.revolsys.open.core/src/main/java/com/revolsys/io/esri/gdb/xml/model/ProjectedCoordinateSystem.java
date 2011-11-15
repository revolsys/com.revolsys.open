package com.revolsys.io.esri.gdb.xml.model;

import com.revolsys.gis.cs.GeometryFactory;

public class ProjectedCoordinateSystem extends SpatialReference {
  public ProjectedCoordinateSystem() {
  }

  public ProjectedCoordinateSystem(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  public ProjectedCoordinateSystem(final GeometryFactory geometryFactory,
    final String wkt) {
    super(geometryFactory, wkt);
  }

}
