package com.revolsys.gis.model.geometry;

import com.revolsys.gis.model.coordinates.Coordinates;

public interface Point extends Coordinates, Geometry {
  double distance(Point point);
}
