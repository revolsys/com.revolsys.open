package com.revolsys.gis.model.geometry.algorithm.locate;

import com.revolsys.gis.model.coordinates.Coordinates;

public interface PointOnGeometryLocator {
  int locate(Coordinates coordinates);
}
