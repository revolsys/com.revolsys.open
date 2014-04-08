package com.revolsys.io.esri.gdb.xml.model;

import com.revolsys.jts.geom.GeometryFactory;

public class ProjectedCoordinateSystem extends SpatialReference {
  public ProjectedCoordinateSystem() {
  }

  public ProjectedCoordinateSystem(final GeometryFactory geometryFactory,
    final String wkt) {
    super(geometryFactory, wkt);
  }

}
