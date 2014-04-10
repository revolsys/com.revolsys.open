package com.revolsys.gis.model.geometry;

import com.revolsys.jts.geom.CoordinatesList;

public interface LineString extends Geometry, CoordinatesList {
  Point getFromPoint();

  Point getToPoint();

  boolean isClosed();
}
