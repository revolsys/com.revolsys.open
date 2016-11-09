package com.revolsys.geometry.model.impl;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public class PointDoubleXYZ extends PointDoubleXY {
  private static final long serialVersionUID = 1L;

  protected final double z;

  public PointDoubleXYZ(final double x, final double y, final double z) {
    super(x, y);
    this.z = z;
  }

  protected PointDoubleXYZ(final GeometryFactory geometryFactory, final double... coordinates) {
    super(coordinates[0], coordinates[1]);
    this.z = geometryFactory.makeZPrecise(coordinates[2]);
  }

  protected PointDoubleXYZ(final GeometryFactory geometryFactory, final double x, final double y,
    final double z) {
    super(x, y);
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
      coordinates[i] = Double.NaN;
    }
  }

  @Override
  public int getAxisCount() {
    return 3;
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      if (axisIndex == X) {
        return this.x;
      } else if (axisIndex == Y) {
        return this.y;
      } else if (axisIndex == Z) {
        return this.z;
      } else {
        return Double.NaN;
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
  public double getX() {
    return this.x;
  }

  @Override
  public double getY() {
    return this.y;
  }

  @Override
  public double getZ() {
    return this.z;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public Point move(final double... deltas) {
    if (deltas == null) {
      return this;
    } else {
      double x = this.x;
      if (deltas.length > 0) {
        x += deltas[0];
      }
      double y = this.y;
      if (deltas.length > 1) {
        y += deltas[1];
      }
      final double z = this.z;
      if (deltas.length > 1) {
        y += deltas[1];
      }
      return new PointDoubleXYZ(x, y, z);
    }
  }
}
