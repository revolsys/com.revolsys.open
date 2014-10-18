package com.revolsys.jts.operation.simple;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.operation.valid.GeometryError;

public class SelfIntersectionPointError extends GeometryError {
  public SelfIntersectionPointError(final Geometry geometry, final Point point) {
    super("Self Intersection at Point", geometry, point);
  }
}
