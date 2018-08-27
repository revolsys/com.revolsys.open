package com.revolsys.geometry.cs.projection;

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.util.function.BiConsumerDouble;
import com.revolsys.util.number.Doubles;

public class CoordinatesOperationPoint {
  public double x;

  public double y;

  public double z;

  public double m;

  public CoordinatesOperationPoint() {
    this(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
  }

  public CoordinatesOperationPoint(final double x, final double y) {
    this.x = x;
    this.y = y;
    this.z = Double.NaN;
    this.m = Double.NaN;
  }

  public CoordinatesOperationPoint(final double x, final double y, final double z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = Double.NaN;
  }

  public CoordinatesOperationPoint(final double x, final double y, final double z, final double m) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = m;
  }

  public CoordinatesOperationPoint(final Point point) {
    this.x = point.getX();
    this.y = point.getY();
    this.z = point.getZ();
    this.m = point.getM();
  }

  public void apply2d(final BiConsumerDouble action) {
    action.accept(this.x, this.y);
  }

  public void copyCoordinatesTo(final double[] coordinates) {
    final int axisCount = coordinates.length;
    coordinates[0] = this.x;
    coordinates[1] = this.y;
    if (axisCount > 2) {
      coordinates[2] = this.z;
    }
    if (axisCount > 3) {
      coordinates[3] = this.m;
    }
  }

  public void copyCoordinatesTo(final double[] coordinates, final int axisCount) {
    coordinates[0] = this.x;
    coordinates[1] = this.y;
    if (axisCount > 2) {
      coordinates[2] = this.z;
    }
    if (axisCount > 3) {
      coordinates[3] = this.m;
    }
  }

  public void copyCoordinatesTo(final double[] coordinates, final int offset, final int axisCount) {
    coordinates[offset] = this.x;
    coordinates[offset + 1] = this.y;
    if (axisCount > 2) {
      coordinates[offset + 2] = this.z;
    }
    if (axisCount > 3) {
      coordinates[offset + 3] = this.m;
    }
  }

  public void resetPoint(final double x, final double y, final double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public void setPoint(final double x, final double y) {
    this.x = x;
    this.y = y;
    this.z = Double.NaN;
    this.m = Double.NaN;
  }

  public void setPoint(final double x, final double y, final double z) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.m = Double.NaN;
  }

  public void setPoint(final double[] coordinates, final int offset, final int axisCount) {
    this.x = coordinates[offset];
    this.y = coordinates[offset + 1];
    if (axisCount > 2) {
      this.z = coordinates[offset + 2];
      if (axisCount > 3) {
        this.m = coordinates[offset + 3];
      }
    }
  }

  public void setPoint(final LineString lineString, final int vertexIndex) {
    this.x = lineString.getCoordinate(0, vertexIndex);
    this.y = lineString.getCoordinate(1, vertexIndex);
    this.z = lineString.getCoordinate(2, vertexIndex);
    this.m = lineString.getCoordinate(3, vertexIndex);
  }

  public void setPoint(final Point point) {
    this.x = point.getX();
    this.y = point.getY();
    this.z = point.getZ();
    this.m = point.getM();
  }

  @Override
  public String toString() {
    if (Double.isFinite(this.m)) {
      return "POINT ZM(" + Doubles.toString(this.x) + " " + Doubles.toString(this.y) + " " + this.z
        + " " + this.m + ")";
    } else if (Double.isFinite(this.z)) {
      return "POINT Z(" + Doubles.toString(this.x) + " " + Doubles.toString(this.y) + " " + this.z
        + ")";
    } else {
      return "POINT(" + Doubles.toString(this.x) + " " + Doubles.toString(this.y) + ")";
    }
  }
}
