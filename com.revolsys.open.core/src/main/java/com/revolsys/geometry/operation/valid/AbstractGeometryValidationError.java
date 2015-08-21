package com.revolsys.geometry.operation.valid;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;

public abstract class AbstractGeometryValidationError implements GeometryValidationError {

  private final String message;

  private final Geometry geometry;

  public AbstractGeometryValidationError(final String message, final Geometry geometry) {
    this.message = message;
    this.geometry = geometry;
  }

  @Override
  public Geometry getErrorGeometry() {
    return getErrorPoint();
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
