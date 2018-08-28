package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class BoundingBoxEmpty extends BaseBoundingBox {

  private static final long serialVersionUID = 1L;

  private final GeometryFactory geometryFactory;

  public BoundingBoxEmpty(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public BoundingBox newBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    return new BoundingBoxDoubleXYGeometryFactory(this.geometryFactory, minX, minY, maxX, maxY);
  }

  @Override
  public BoundingBox newBoundingBox(final int axisCount, final double... bounds) {
    if (axisCount == 2) {
      final double x1 = bounds[0];
      final double y1 = bounds[1];
      final double x2 = bounds[2];
      final double y2 = bounds[3];
      return new BoundingBoxDoubleXYGeometryFactory(this.geometryFactory, x1, y1, x2, y2);
    } else {
      return new BoundingBoxDoubleGf(this.geometryFactory, axisCount, bounds);
    }
  }

  @Override
  public BoundingBox newBoundingBoxEmpty() {
    return this;
  }
}
