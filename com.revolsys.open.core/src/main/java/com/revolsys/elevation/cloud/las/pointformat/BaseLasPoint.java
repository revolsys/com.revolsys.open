package com.revolsys.elevation.cloud.las.pointformat;

import java.io.Serializable;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.cloud.las.LasPointCloud;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.AbstractPoint;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;
import com.revolsys.util.function.BiConsumerDouble;

public abstract class BaseLasPoint extends AbstractPoint implements LasPoint, Serializable {
  private static final long serialVersionUID = 1L;

  private int x = Integer.MIN_VALUE;

  private int y = Integer.MIN_VALUE;

  private int z = Integer.MIN_VALUE;

  private final LasPointCloud pointCloud;

  public BaseLasPoint(final LasPointCloud pointCloud) {
    this.pointCloud = pointCloud;
  }

  /**
   * Creates and returns a full copy of this {@link Point} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public BaseLasPoint clone() {
    return (BaseLasPoint)super.clone();
  }

  @Override
  public void copyCoordinates(final double[] coordinates) {
    coordinates[X] = getX();
    coordinates[Y] = getY();
    if (coordinates.length > 2) {
      coordinates[Z] = getZ();
    }
  }

  @Override
  public double distancePoint(Point point) {
    if (isEmpty()) {
      return java.lang.Double.POSITIVE_INFINITY;
    } else if (Property.isEmpty(point)) {
      return java.lang.Double.POSITIVE_INFINITY;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertPoint2d(geometryFactory);
      final double x = point.getX();
      final double y = point.getY();
      final double x1 = getX();
      final double y1 = this.y;
      return MathUtil.distance(x1, y1, x, y);
    }
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
  public void forEachVertex(final BiConsumerDouble action) {
    action.accept(this.x, this.y);
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
        return this.pointCloud.toDoubleX(this.x);
      } else if (axisIndex == Y) {
        return this.pointCloud.toDoubleY(this.y);
      } else if (axisIndex == Z) {
        return this.pointCloud.toDoubleZ(this.z);
      } else {
        return java.lang.Double.NaN;
      }
    }
  }

  @Override
  public double[] getCoordinates() {
    return new double[] {
      getX(), getY(), getZ()
    };
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.pointCloud.getGeometryFactory();
  }

  public LasPointCloud getPointCloud() {
    return this.pointCloud;
  }

  @Override
  public double getX() {
    return this.pointCloud.toDoubleX(this.x);
  }

  public int getXInt() {
    return this.x;
  }

  @Override
  public double getY() {
    return this.pointCloud.toDoubleY(this.y);
  }

  public int getYInt() {
    return this.y;
  }

  @Override
  public double getZ() {
    return this.pointCloud.toDoubleZ(this.z);
  }

  public int getZInt() {
    return this.z;
  }

  @Override
  public int hashCode() {
    long bits = java.lang.Double.doubleToLongBits(getX());
    bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
    return (int)bits ^ (int)(bits >> 32);
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public void setLocation(final double x, final double y) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setXYZ(final int x, final int y, final int z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addToMap(map, "x", getX());
    addToMap(map, "y", getY());
    addToMap(map, "z", getZ());
    return map;
  }

  @Override
  public String toString() {
    return toEwkt();
  }
}
