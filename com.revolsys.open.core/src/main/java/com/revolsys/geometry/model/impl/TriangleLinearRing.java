package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.util.function.BiConsumerDouble;

public class TriangleLinearRing extends AbstractLineString implements LinearRing {
  private static final long serialVersionUID = 1L;

  private final Triangle triangle;

  public TriangleLinearRing(final Triangle triangle) {
    this.triangle = triangle;
  }

  @Override
  public TriangleLinearRing clone() {
    return (TriangleLinearRing)super.clone();
  }

  @Override
  public void forEachVertex(final BiConsumerDouble action) {
    if (!isEmpty()) {
      final double x1 = this.triangle.getX(0);
      final double y1 = this.triangle.getY(0);
      action.accept(x1, y1);
      final double x2 = this.triangle.getX(1);
      final double y2 = this.triangle.getY(1);
      action.accept(x2, y2);
      final double x3 = this.triangle.getX(2);
      final double y3 = this.triangle.getY(2);
      action.accept(x3, y3);
    }
  }

  @Override
  public int getAxisCount() {
    return this.triangle.getAxisCount();
  }

  @Override
  public double getCoordinate(int vertexIndex, final int axisIndex) {
    if (vertexIndex == 3) {
      vertexIndex = 0;
    }
    return this.triangle.getCoordinate(vertexIndex, axisIndex);
  }

  @Override
  public double[] getCoordinates() {
    return this.triangle.getCoordinates();
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.triangle.getGeometryFactory();
  }

  @Override
  public int getVertexCount() {
    return 4;
  }

  @Override
  public LinearRing newGeometry(final GeometryFactory geometryFactory) {
    return LinearRing.super.newGeometry(geometryFactory);
  }
}
