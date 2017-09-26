package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.number.Doubles;

public class PointEditor extends AbstractGeometryEditor implements Point, PunctualEditor {
  private static final long serialVersionUID = 1L;

  private final Point point;

  private double[] newCoordinates;

  public PointEditor(final AbstractGeometryEditor parentEditor, final Point point) {
    super(parentEditor, point);
    this.point = point;
  }

  public PointEditor(final Point point) {
    this(null, point);
  }

  @Override
  public Point clone() {
    return (Point)super.clone();
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return equals(point);
    } else {
      return false;
    }
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (this.newCoordinates == null) {
      return this.point.getCoordinate(axisIndex);
    } else {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        return this.newCoordinates[axisIndex];
      } else {
        return java.lang.Double.NaN;
      }
    }
  }

  @Override
  public double getX() {
    if (this.newCoordinates == null) {
      return this.point.getX();
    } else {
      return this.newCoordinates[X];
    }
  }

  @Override
  public double getY() {
    if (this.newCoordinates == null) {
      return this.point.getY();
    } else {
      return this.newCoordinates[Y];
    }
  }

  @Override
  public int hashCode() {
    final double x = getX();
    final double y = getY();
    long bits = java.lang.Double.doubleToLongBits(x);
    bits ^= java.lang.Double.doubleToLongBits(y) * 31;
    return (int)bits ^ (int)(bits >> 32);
  }

  @Override
  public boolean isEmpty() {
    return this.point.isEmpty();
  }

  @Override
  public Point newGeometry() {
    if (this.newCoordinates == null) {
      return this.point;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return this.point.newPoint(geometryFactory, this.newCoordinates);
    }
  }

  @Override
  public Point newPoint(final GeometryFactory geometryFactory, final double... coordinates) {
    return this.point.newPoint(geometryFactory, coordinates);
  }

  @Override
  public int setAxisCount(final int axisCount) {
    final int oldAxisCount = getAxisCount();
    if (oldAxisCount != axisCount) {
      this.newCoordinates = getCoordinates(axisCount);
      super.setAxisCount(oldAxisCount);
    }
    return oldAxisCount;
  }

  public double setCoordinate(final int axisIndex, final double coordinate) {
    final double oldValue = this.point.getCoordinate(axisIndex);
    if (!Doubles.equal(coordinate, oldValue)) {
      final int axisCount = getAxisCount();
      if (axisIndex >= 0 && axisIndex < axisCount) {
        if (this.newCoordinates == null) {
          this.newCoordinates = this.point.getCoordinates(axisCount);
        }
        this.newCoordinates[axisIndex] = coordinate;
      }
    }
    return oldValue;
  }

  @Override
  public double setCoordinate(final int axisIndex, final double coordinate, final int... vertexId) {
    if (vertexId.length == 0) {
      return setCoordinate(axisIndex, coordinate);
    } else {
      return java.lang.Double.NaN;
    }
  }

  @Override
  public double setCoordinate(final int partIndex, final int axisIndex, final double coordinate) {
    if (partIndex == 0) {
      return setCoordinate(axisIndex, coordinate);
    } else {
      return Double.NaN;
    }
  }

  public PointEditor setM(final double m) {
    setCoordinate(M, m);
    return this;
  }

  public PointEditor setX(final double x) {
    setCoordinate(X, x);
    return this;
  }

  public PointEditor setY(final double y) {
    setCoordinate(Y, y);
    return this;
  }

  @Override
  public PointEditor setZ(final double z) {
    setCoordinate(Z, z);
    return this;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
