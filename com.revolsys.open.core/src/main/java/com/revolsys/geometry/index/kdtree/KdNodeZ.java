package com.revolsys.geometry.index.kdtree;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;

public class KdNodeZ extends KdNode {
  private static final long serialVersionUID = 1L;

  private double z = java.lang.Double.NaN;

  public KdNodeZ(final double x, final double y) {
    super(x, y);
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
      double z = this.z;
      if (deltas.length > 1) {
        z += deltas[1];
      }
      return new PointDoubleXYZ(x, y, z);
    }
  }

  @Override
  public Point setZ(final double z) {
    this.z = z;
    return this;
  }

  /**
   * Update the z value using an average of all the z values for points at this location.
   *
   * this.z = (this.z * (count-1) + z) / count
   * @param z
   */
  public void setZAverage(final double z) {
    if (java.lang.Double.isFinite(z)) {
      final double count = getCount();
      if (java.lang.Double.isFinite(this.z) && count > 0) {
        this.z = (this.z * (count - 1) + z) / count;
      } else {
        this.z = z;
      }
    }
  }
}
