package com.revolsys.gis.esri.gdb.xml.model;

import com.revolsys.gis.cs.GeometryFactory;

public class GeographicCoordinateSystem extends SpatialReference {

  public GeographicCoordinateSystem() {
  }

  public GeographicCoordinateSystem(GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

public GeographicCoordinateSystem(GeometryFactory geometryFactory, String wkt) {
	super(geometryFactory, wkt);
}

}
