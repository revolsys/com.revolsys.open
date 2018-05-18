package com.revolsys.geometry.model.util;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXYGeometryFactory;
import com.revolsys.util.function.BiConsumerDouble;

public class BoundingBoxXyConstructor implements BiConsumerDouble {

  private double minX = Double.POSITIVE_INFINITY;

  private double minY = Double.POSITIVE_INFINITY;

  private double maxX = Double.NEGATIVE_INFINITY;

  private double maxY = Double.NEGATIVE_INFINITY;

  private final GeometryFactory geometryFactory;

  public BoundingBoxXyConstructor(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  @Override
  public void accept(final double x, final double y) {
    if (x < this.minX) {
      this.minX = x;
    }
    if (x > this.maxX) {
      this.maxX = x;
    }
    if (y < this.minY) {
      this.minY = y;
    }
    if (y > this.maxY) {
      this.maxY = y;
    }
  }

  public BoundingBox newBoundingBox() {
    return new BoundingBoxDoubleXYGeometryFactory(this.geometryFactory, this.minX, this.minY,
      this.maxX, this.maxY);
  }
}
