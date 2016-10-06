package com.revolsys.elevation.tin;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.HCoordinate;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.AbstractPolygon;
import com.revolsys.geometry.model.impl.Circle;
import com.revolsys.util.MathUtil;

public class TriangleWithCircumcircle extends AbstractPolygon implements Triangle {

  public static Triangle newClockwiseTriangle(final double x1, final double y1, final double x2,
    final double y2, final double x3, final double y3) {
    return newClockwiseTriangle(x1, y1, Double.NaN, x2, y2, Double.NaN, x3, y3, Double.NaN);
  }

  public static TriangleWithCircumcircle newClockwiseTriangle(final double x1, final double y1,
    final double z1, final double x2, final double y2, final double z2, final double x3,
    final double y3, final double z3) {
    if (CoordinatesListUtil.orientationIndex(x1, y1, x2, y2, x3, y3) == CGAlgorithms.CLOCKWISE) {
      return new TriangleWithCircumcircle(//
        x1, y1, z1, //
        x2, y2, z2, //
        x3, y3, z3);
    } else {
      return new TriangleWithCircumcircle(//
        x1, y1, z1, //
        x3, y3, z3, //
        x2, y2, z2);
    }
  }

  public static TriangleWithCircumcircle newClockwiseTriangle(final Point p1, final Point p2,
    final Point p3) {
    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double z1 = p1.getZ();

    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double z2 = p2.getZ();

    final double x3 = p3.getX();
    final double y3 = p3.getY();
    final double z3 = p3.getZ();

    return newClockwiseTriangle(x1, y1, z1, x2, y2, z2, x3, y3, z3);
  }

  public static TriangleWithCircumcircle newTriangle(final Point p1, final Point p2,
    final Point p3) {

    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double z1 = p1.getZ();

    final double x2 = p2.getX();
    final double y2 = p2.getY();
    final double z2 = p2.getZ();

    final double x3 = p3.getX();
    final double y3 = p3.getY();
    final double z3 = p3.getZ();
    return new TriangleWithCircumcircle(//
      x1, y1, z1, //
      x2, y2, z2, //
      x3, y3, z3);
  }

  private final double x1;

  private final double y1;

  private final double z1;

  private final double x2;

  private final double y2;

  private final double z2;

  private final double x3;

  private final double y3;

  private final double z3;

  private double centreX = Double.NaN;

  private double centreY = Double.NaN;

  private final double radius;

  public TriangleWithCircumcircle(final double x1, final double y1, final double z1,
    final double x2, final double y2, final double z2, final double x3, final double y3,
    final double z3) {
    this.x1 = x1;
    this.y1 = y1;
    this.z1 = z1;
    this.x2 = x2;
    this.y2 = y2;
    this.z2 = z2;
    this.x3 = x3;
    this.y3 = y3;
    this.z3 = z3;
    final HCoordinate hcc = CoordinatesUtil.getCircumcentreHCoordinate(x1, y1, x2, y2, x3, y3);
    try {
      this.centreX = hcc.getX();
      this.centreY = hcc.getY();
    } catch (final Throwable e) {
    }
    this.radius = Triangle.getCircumcircleRadius(x1, y1, x2, y2, x3, y3);
  }

  @Override
  public boolean circumcircleContains(final double x, final double y) {
    final double distanceFromCentre = MathUtil.distance(this.centreX, this.centreY, x, y);
    return distanceFromCentre < this.radius + 0.0001;
  }

  @Override
  public TriangleWithCircumcircle clone() {
    return (TriangleWithCircumcircle)super.clone();
  }

  @Override
  public int getAxisCount() {
    return 3;
  }

  @Override
  public Point getCircumcentre() {
    return getGeometryFactory().point(this.centreX, this.centreY);
  }

  @Override
  public Circle getCircumcircle() {
    final Point circumcentre = getCircumcentre();
    return new Circle(circumcentre, this.radius);
  }

  @Override
  public double getCircumcircleRadius() {
    return this.radius;
  }

  @Override
  public double getCoordinate(int vertexIndex, final int axisIndex) {
    if (axisIndex < 3) {
      while (vertexIndex < 0) {
        vertexIndex += 4;
      }
      if (vertexIndex >= 3) {
        vertexIndex = vertexIndex % 4;
        if (vertexIndex == 3) {
          vertexIndex = 0;
        }
      }
      switch (vertexIndex) {
        case 0:
          switch (axisIndex) {
            case X:
              return this.x1;
            case Y:
              return this.y1;
            case Z:
              return this.z1;
          }
        break;
        case 1:
          switch (axisIndex) {
            case X:
              return this.x2;
            case Y:
              return this.y2;
            case Z:
              return this.z2;
          }
        break;
        case 2:
          switch (axisIndex) {
            case X:
              return this.x3;
            case Y:
              return this.y3;
            case Z:
              return this.z3;
          }
        break;
      }
    }
    return Double.NaN;
  }

  @Override
  public double[] getCoordinates() {
    return new double[] {
      this.x1, this.y1, this.z1, this.x2, this.y2, this.z2, this.x3, this.y3, this.z3
    };
  }

  public double getX1() {
    return this.x1;
  }

  public double getX2() {
    return this.x2;
  }

  public double getX3() {
    return this.x3;
  }

  public double getY1() {
    return this.y1;
  }

  public double getY2() {
    return this.y2;
  }

  public double getY3() {
    return this.y3;
  }

  public double getZ1() {
    return this.z1;
  }

  public double getZ2() {
    return this.z2;
  }

  public double getZ3() {
    return this.z3;
  }

}
