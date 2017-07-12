package com.revolsys.geometry.model.editor;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.AbstractPoint;
import com.revolsys.util.number.Doubles;

public class PointEditor extends AbstractPoint implements PunctualEditor {
  private static final long serialVersionUID = 1L;

  private final Point point;

  private double[] newCoordinates;

  private GeometryFactory newGeometryFactory;

  public PointEditor(final Point point) {
    this.point = point;
    this.newGeometryFactory = point.getGeometryFactory();
  }

  @Override
  public int getAxisCount() {
    return this.newGeometryFactory.getAxisCount();
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
  public GeometryFactory getGeometryFactory() {
    return this.newGeometryFactory;
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
  public boolean isEmpty() {
    return this.point.isEmpty();
  }

  @Override
  public Point newGeometry() {
    if (this.newCoordinates == null) {
      return this.point;
    } else {
      return this.point.newPoint(this.newGeometryFactory, this.newCoordinates);
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
      this.newGeometryFactory = this.newGeometryFactory.convertAxisCount(axisCount);
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
      return java.lang.Double.NaN;
    }
  }

  public double setM(final double m) {
    return setCoordinate(M, m);
  }

  public double setX(final double x) {
    return setCoordinate(X, x);
  }

  public double setY(final double y) {
    return setCoordinate(Y, y);
  }

  public double setZ(final double z) {
    return setCoordinate(Z, z);
  }

  @Override
  public String toString() {
    return toEwkt();
  }

}
