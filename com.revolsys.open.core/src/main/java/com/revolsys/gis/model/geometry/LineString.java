package com.revolsys.gis.model.geometry;

import com.revolsys.gis.model.coordinates.list.CoordinatesList;

public interface LineString extends Geometry, CoordinatesList {
  Point getFromPoint();

  Point getToPoint();

  boolean isClosed();
}
