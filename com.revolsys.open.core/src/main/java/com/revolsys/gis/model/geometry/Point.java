package com.revolsys.gis.model.geometry;

import com.revolsys.jts.geom.Coordinates;

public interface Point extends Coordinates, Geometry {
  double distance(Point point);
}
