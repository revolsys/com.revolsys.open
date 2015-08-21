package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public interface GeometryValidationError {

  Geometry getErrorGeometry();

  Point getErrorPoint();

  Geometry getGeometry();

  String getMessage();
}
