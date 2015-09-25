package com.revolsys.record.io.format.esri.gdb.xml.model;

import com.revolsys.geometry.model.GeometryFactory;

public class ProjectedCoordinateSystem extends SpatialReference {
  public ProjectedCoordinateSystem() {
  }

  public ProjectedCoordinateSystem(final GeometryFactory geometryFactory, final String wkt) {
    super(geometryFactory, wkt);
  }

}
