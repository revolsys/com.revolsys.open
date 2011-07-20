package com.revolsys.gis.esri.gdb.xml.model;

import com.revolsys.gis.cs.GeometryFactory;

public class ProjectedCoordinateSystem extends SpatialReference {
  public ProjectedCoordinateSystem() {
  }

  public ProjectedCoordinateSystem(GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

public ProjectedCoordinateSystem(GeometryFactory geometryFactory, String wkt) {
	super(geometryFactory, wkt);
}

}
