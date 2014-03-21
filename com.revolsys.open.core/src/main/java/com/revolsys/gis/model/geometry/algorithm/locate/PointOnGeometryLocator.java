package com.revolsys.gis.model.geometry.algorithm.locate;

import com.revolsys.gis.model.coordinates.Coordinates;

public interface PointOnGeometryLocator {
  Location locate(Coordinates coordinates);

  Location locate(final double x, final double y);
}
