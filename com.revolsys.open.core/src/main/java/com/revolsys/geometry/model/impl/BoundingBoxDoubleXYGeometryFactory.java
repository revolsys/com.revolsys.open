package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;

public class BoundingBoxDoubleXYGeometryFactory extends BoundingBoxDoubleXY {

  private static final long serialVersionUID = 1L;

  private final GeometryFactory geometryFactory;

  public BoundingBoxDoubleXYGeometryFactory(final GeometryFactory geometryFactory, final double x,
    final double y) {
    super(x, y);
    this.geometryFactory = geometryFactory;
  }

  public BoundingBoxDoubleXYGeometryFactory(final GeometryFactory geometryFactory, final double x1,
    final double y1, final double x2, final double y2) {
    super(geometryFactory, x1, y1, x2, y2);
    this.geometryFactory = geometryFactory;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public BoundingBox newBoundingBox(final double minX, final double minY, final double maxX,
    final double maxY) {
    return new BoundingBoxDoubleXYGeometryFactory(this.geometryFactory, minX, minY, maxX, maxY);
  }

  @Override
  public RectangleXY toRectangle() {
    final double width = getWidth();
    final double height = getHeight();
    return this.geometryFactory.newRectangle(this.minX, this.minY, width, height);
  }
}
