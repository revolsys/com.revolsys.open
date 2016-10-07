package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class BoundingBoxGeometryFactory implements BoundingBox {

  private final GeometryFactory geometryFactory;

  public BoundingBoxGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public BoundingBox clone() {
    return this;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof BoundingBox) {
      final BoundingBox boundingBox = (BoundingBox)other;
      return equals(boundingBox);
    } else {
      return false;
    }
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public BoundingBox newBoundingBox(final double x, final double y) {
    return new BoundingBoxDoubleXYGeometryFactory(this.geometryFactory, x, y);
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

  @Override
  public String toString() {
    return BoundingBox.toString(this);
  }
}
