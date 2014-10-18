package com.revolsys.jts.operation.valid;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.Point;

public class GeometryError extends AbstractGeometryValidationError {

  private final Geometry errorGeometry;

  public GeometryError(final String message, final Geometry geometry,
    final Geometry errorGeometry) {
    super(message, geometry);
    this.errorGeometry = errorGeometry;
  }

  @Override
  public Geometry getErrorGeometry() {
    return this.errorGeometry;
  }

  @Override
  public Point getErrorPoint() {
    return this.errorGeometry.getPoint();
  }
}
