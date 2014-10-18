package com.revolsys.jts.operation.valid;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;

public interface GeometryValidationError {

  Geometry getErrorGeometry();

  Point getErrorPoint();

  Geometry getGeometry();

  String getMessage();
}
