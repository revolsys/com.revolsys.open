package com.revolsys.geometry.model.impl;

import java.util.function.Consumer;

import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public class PointDoubleXYZ extends PointDoubleXY {
  private static final long serialVersionUID = 1L;

  protected double z;

  protected PointDoubleXYZ() {
    this.z = java.lang.Double.NaN;
  }

  public PointDoubleXYZ(final double x, final double y, final double z) {
    super(x, y);
    this.z = z;
  }

  protected PointDoubleXYZ(final GeometryFactory geometryFactory, final double x, final double y,
    final double z) {
    super(geometryFactory, x, y);
    this.z = geometryFactory.makeZPrecise(z);
  }

  @Override
  public PointDoubleXYZ clone() {
    return (PointDoubleXYZ)super.clone();
  }

  @Override
  public void copyCoordinates(final double[] coordinates) {
    coordinates[X] = this.x;
    coordinates[Y] = this.y;
    coordinates[Z] = this.z;
    for (int i = 3; i < coordinates.length; i++) {
      coordinates[i] = java.lang.Double.NaN;
    }
  }

  @Override
  public void forEachVertex(final CoordinatesOperation coordinatesOperation,
    final double[] coordinates, final Consumer<double[]> action) {
    if (!isEmpty()) {
      coordinates[X] = this.x;
      coordinates[Y] = this.y;
      if (coordinates.length > 2) {
        coordinates[Z] = this.z;
        coordinatesOperation.perform(3, coordinates, 3, coordinates);
      } else {
        coordinatesOperation.perform(2, coordinates, 2, coordinates);
      }
      action.accept(coordinates);
    }
  }

  @Override
  public void forEachVertex(final double[] coordinates, final Consumer<double[]> action) {
    if (!isEmpty()) {
      coordinates[X] = this.x;
      coordinates[Y] = this.y;
      if (coordinates.length > 2) {
        coordinates[Z] = this.z;
      }
      action.accept(coordinates);
    }
  }

  @Override
  public int getAxisCount() {
    return 3;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return java.lang.Double.NaN;
    } else {
      if (axisIndex == X) {
        return this.x;
      } else if (axisIndex == Y) {
        return this.y;
      } else if (axisIndex == Z) {
        return this.z;
      } else {
        return java.lang.Double.NaN;
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    return new double[] {
      this.x, this.y, this.z
    };
  }

  @Override
  public double getZ() {
    return this.z;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  public Point newPoint(final double x, final double y, final double z) {
    return new PointDoubleXYZ(x, y, z);
  }
}
