package com.revolsys.jts.operation.valid;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;

public abstract class AbstractGeometryValidationError implements
GeometryValidationError {

  private final String message;

  private final Geometry geometry;

  public AbstractGeometryValidationError(final String message,
    final Geometry geometry) {
    this.message = message;
    this.geometry = geometry;
  }

  @Override
  public Point getErrorPoint() {
    return this.geometry.getPoint();
  }

  @Override
  public Geometry getGeometry() {
    return this.geometry;
  }

  @Override
  public String getMessage() {
    return this.message;
  }

  @Override
  public String toString() {
    return this.message;
  }
}
