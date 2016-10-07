package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Triangle;

public class TriangleLinearRing extends AbstractLineString implements LinearRing {
  /**
   *
   */
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
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    // TODO Auto-generated method stub
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
    // TODO Auto-generated method stub
    return 4;
  }

}
