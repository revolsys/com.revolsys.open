/*
 * The JTS Topology Suite is a collection of Java classes that
 * implement the fundamental operations required to validate a given
 * geo-spatial data set to a known topological specification.
 *
 * Copyright (C) 2001 Vivid Solutions
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * For more information, contact:
 *
 *     Vivid Solutions
 *     Suite #1A
 *     2328 Government Street
 *     Victoria BC  V8T 5G5
 *     Canada
 *
 *     (250)385-6040
 *     www.vividsolutions.com
 */
package com.revolsys.geometry.model;

import java.io.Serializable;
import java.util.Arrays;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.PointVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.util.NumberUtil;
import com.revolsys.math.Angle;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;
import com.revolsys.util.number.Doubles;

/**
 * Represents a single point.
 *
 * A <code>Point</code> is topologically valid if and only if:
 * <ul>
 * <li>the coordinate which defines it (if any) is a valid coordinate
 * (i.e does not have an <code>NaN</code> X or Y ordinate)
 * </ul>
 *
 *@version 1.7
 */
public interface Point extends Punctual, Serializable {
  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newPoint(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Point) {
      return (G)value;
    } else if (value instanceof Geometry) {
      throw new IllegalArgumentException(
        ((Geometry)value).getGeometryType() + " cannot be converted to a Point");
    } else {
      final String string = DataTypes.toString(value);
      return (G)GeometryFactory.DEFAULT.geometry(string, false);
    }
  }

  /**
   * Calculate the counter clockwise angle in radians of the vector from this
   * point to another point. The angle is relative to the positive x-axis
   * relative to the positive X-axis. The angle will be in the range -PI -> PI
   * where negative values have a clockwise orientation.
   *
   * @return The angle in radians.
   */
  default double angle2d(final Point other) {
    final double x1 = this.getX();
    final double y1 = this.getY();
    final double x2 = other.getX();
    final double y2 = other.getY();
    return Angle.angle2d(x1, x2, y1, y2);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V appendVertex(final Point newPoint, final int... geometryId) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else if (isEmpty()) {
      return newPoint.convertGeometry(geometryFactory);
    } else if (newPoint.isEmpty()) {
      return (V)this;
    } else {
      return (V)geometryFactory.lineString(this, newPoint);
    }
  }

  @Override
  Point clone();

  @Override
  default int compareTo(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return compareTo(point);
    } else if (other instanceof Geometry) {
      final Geometry geometry = (Geometry)other;
      return compareTo(geometry);
    } else {
      return -1;
    }
  }

  /**
   *  Compares this {@link Coordinates} with the specified {@link Coordinates} for order.
   *  This method ignores the z value when making the comparison.
   *  Returns:
   *  <UL>
   *    <LI> -1 : this.x < other.x || ((this.x == other.x) && (this.y <
   *    other.y))
   *    <LI> 0 : this.x == other.x && this.y = other.y
   *    <LI> 1 : this.x > other.x || ((this.x == other.x) && (this.y > other.y))
   *
   *  </UL>
   *  Note: This method assumes that ordinate values
   * are valid numbers.  NaN values are not handled correctly.
   *
   *@param  o  the <code>Coordinate</code> with which this <code>Coordinate</code>
   *      is being compared
   *@return    -1, zero, or 1 as this <code>Coordinate</code>
   *      is less than, equal to, or greater than the specified <code>Coordinate</code>
   */
  default int compareTo(final Point point) {
    final boolean otherEmpty = point.isEmpty();
    if (isEmpty()) {
      if (otherEmpty) {
        return 0;
      } else {
        return -1;
      }
    } else if (otherEmpty) {
      return 1;
    } else {
      final double x1 = this.getX();
      final double y1 = this.getY();
      final double x2 = point.getX();
      final double y2 = point.getY();
      return CoordinatesUtil.compare(x1, y1, x2, y2);
    }
  }

  @Override
  default int compareToSameClass(final Geometry other) {
    final Point point = (Point)other;
    return getPoint().compareTo(point.getPoint());
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V copy(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return (V)this.clone();
    } else if (isEmpty()) {
      return (V)geometryFactory.point();
    } else {
      geometryFactory = Geometry.getNonZeroGeometryFactory(this, geometryFactory);
      double[] targetCoordinates;
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory
        .getCoordinatesOperation(geometryFactory);
      final double[] coordinates = getCoordinates();
      if (coordinatesOperation == null) {
        targetCoordinates = coordinates;
      } else {
        final int sourceAxisCount = getAxisCount();
        final int targetAxisCount = geometryFactory.getAxisCount();
        targetCoordinates = new double[targetAxisCount];
        coordinatesOperation.perform(sourceAxisCount, coordinates, targetAxisCount,
          targetCoordinates);
      }

      return (V)geometryFactory.point(targetCoordinates);
    }
  }

  /**
   * Copy the coordinates in this point to the coordinates array parameter and convert them to the geometry factory.
   *
   * @param geometryFactory
   * @param coordinates
   */
  default void copyCoordinates(GeometryFactory geometryFactory, final double[] coordinates) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      for (int i = 0; i < coordinates.length; i++) {
        final double value = getCoordinate(i);
        coordinates[i] = value;
      }
    } else if (isEmpty()) {
      for (int i = 0; i < coordinates.length; i++) {
        coordinates[i] = Double.NaN;
      }
    } else {
      geometryFactory = Geometry.getNonZeroGeometryFactory(this, geometryFactory);
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory
        .getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        for (int i = 0; i < coordinates.length; i++) {
          final double value = getCoordinate(i);
          coordinates[i] = value;
        }
      } else {
        final int sourceAxisCount = getAxisCount();
        final int targetAxisCount = geometryFactory.getAxisCount();
        coordinatesOperation.perform(sourceAxisCount, getCoordinates(), targetAxisCount,
          coordinates);
      }

    }
  }

  default int copyCoordinates(final int axisCount, final double nanValue,
    final double[] destCoordinates, int destOffset) {
    if (isEmpty()) {
      return destOffset;
    } else {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        double coordinate = getCoordinate(axisIndex);
        if (Double.isNaN(coordinate)) {
          coordinate = nanValue;
        }
        destCoordinates[destOffset++] = coordinate;
      }
      return destOffset;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V deleteVertex(final int... vertexId) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return (V)geometryFactory.point();
  }

  default double distance(final double x, final double y) {
    final double x1 = this.getX();
    final double y1 = this.getY();
    return MathUtil.distance(x1, y1, x, y);
  }

  @Override
  default double distance(final Geometry geometry, final double terminateDistance) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return distance(point);
    } else if (isEmpty()) {
      return 0.0;
    } else if (Property.isEmpty(geometry)) {
      return 0.0;
    } else {
      return geometry.distance(this);
    }
  }

  /**
   * Computes the 2-dimensional Euclidean distance to another location.
   * The Z-ordinate is ignored.
   *
   * @param c a point
   * @return the 2-dimensional Euclidean distance between the locations
   */
  default double distance(Point point) {
    if (isEmpty()) {
      return 0;
    } else if (Property.isEmpty(point)) {
      return 0.0;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertGeometry(geometryFactory, 2);
      final double x2 = point.getX();
      final double y2 = point.getY();
      return distance(x2, y2);
    }
  }

  /**
   * Computes the 3-dimensional Euclidean distance to another location.
   *
   * @param c a coordinate
   * @return the 3-dimensional Euclidean distance between the locations
   */
  default double distance3d(final Point c) {
    final double dx = getX() - c.getX();
    final double dy = getY() - c.getY();
    final double dz = getZ() - c.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  default boolean equals(final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double coordinate2 = coordinates[i];
      final double coordinate = getCoordinate(i);
      if (!Doubles.equal(coordinate, coordinate2)) {
        return false;
      }
    }
    return true;
  }

  @Override
  default boolean equals(final int axisCount, final Geometry geometry) {
    if (geometry == this) {
      return true;
    } else if (geometry == null) {
      return false;
    } else if (axisCount < 2) {
      throw new IllegalArgumentException("Axis Count must be >=2");
    } else if (isEquivalentClass(geometry)) {
      if (isEmpty()) {
        return geometry.isEmpty();
      } else if (geometry.isEmpty()) {
        return false;
      } else {
        final Point point = (Point)geometry;
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          final double value = getCoordinate(axisIndex);
          final double value2 = point.getCoordinate(axisIndex);
          if (!Doubles.equal(value, value2)) {
            return false;
          }
        }
      }
      return true;
    } else {
      return false;
    }
  }

  default boolean equals(final Point point) {
    if (point == null) {
      return false;
    } else if (isEmpty()) {
      return point.isEmpty();
    } else if (point.isEmpty()) {
      return false;
    } else {
      return equals(2, point);
    }
  }

  /**
   * Tests if another coordinate has the same values for the X and Y ordinates.
   * The Z ordinate is ignored.
   *
   *@param other a <code>Coordinate</code> with which to do the 2D comparison.
   *@return true if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for X and Y.
   */
  default boolean equals2d(final Point point, final double tolerance) {
    if (!NumberUtil.equalsWithTolerance(this.getX(), point.getX(), tolerance)) {
      return false;
    }
    if (!NumberUtil.equalsWithTolerance(this.getY(), point.getY(), tolerance)) {
      return false;
    }
    return true;
  }

  @Override
  default boolean equalsExact(final Geometry other, final double tolerance) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      if (isEmpty() && other.isEmpty()) {
        return true;
      } else if (isEmpty() != other.isEmpty()) {
        return false;
      } else {
        return equal(point, getPoint(), tolerance);
      }
    } else {
      return false;
    }
  }

  /**
   * Gets the boundary of this geometry.
   * Zero-dimensional geometries have no boundary by definition,
   * so an empty GeometryCollection is returned.
   *
   * @return an empty GeometryCollection
   * @see Geometry#getBoundary
   */
  @Override
  default Geometry getBoundary() {
    return getGeometryFactory().geometryCollection();
  }

  @Override
  default int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  /**
   * Gets the ordinate value for the given index.
   * The supported values for the index are
   * {@link #X}, {@link #Y}, and {@link #Z}.
   *
   * @param axisIndex the ordinate index
   * @return the value of the ordinate
   * @throws IllegalArgumentException if the index is not valid
   */
  double getCoordinate(int axisIndex);

  @Override
  default double getCoordinate(final int partIndex, final int axisIndex) {
    if (partIndex == 0) {
      return getCoordinate(axisIndex);
    } else {
      return Double.NaN;
    }
  }

  default double[] getCoordinates() {
    final double[] coordinates = new double[this.getAxisCount()];
    for (int i = 0; i < coordinates.length; i++) {
      coordinates[i] = this.getCoordinate(i);
    }
    return coordinates;
  }

  @Override
  default DataType getDataType() {
    return DataTypes.POINT;
  }

  @Override
  default int getDimension() {
    return 0;
  }

  default double getM() {
    return getCoordinate(3);
  }

  @Override
  default Point getPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return this;
    }
  }

  @Override
  default Point getPoint(final int i) {
    if (i == 0) {
      return this;
    } else {
      return null;
    }
  }

  @Override
  default Point getPointWithin() {
    return getGeometryFactory().convertAxisCount(2).point(this);
  }

  @Override
  default Segment getSegment(final int... segmentId) {
    return null;
  }

  default long getTime() {
    return (long)getM();
  }

  @Override
  default Vertex getToVertex(final int... vertexId) {
    return getVertex(vertexId);
  }

  @Override
  default Vertex getVertex(final int... vertexId) {
    if (isEmpty()) {
      return null;
    } else {
      return new PointVertex(this);
    }
  }

  @Override
  default int getVertexCount() {
    return isEmpty() ? 0 : 1;
  }

  default double getX() {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      return getCoordinate(0);
    }
  }

  default double getY() {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      return getCoordinate(1);
    }
  }

  default double getZ() {
    return getCoordinate(2);
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V insertVertex(final Point newPoint, final int... vertexId) {
    if (vertexId.length == 1) {
      final int vertexIndex = vertexId[0];
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (newPoint == null || newPoint.isEmpty()) {
        return (V)this;
      } else if (isEmpty()) {
        return newPoint.convertGeometry(geometryFactory);
      } else if (newPoint.isEmpty()) {
        return (V)this;
      } else if (vertexIndex == 0) {
        return (V)geometryFactory.lineString(newPoint, this);
      } else {
        return (V)geometryFactory.lineString(this, newPoint);
      }
    } else {
      throw new IllegalArgumentException("Vertex id's for " + getGeometryType()
        + " must have length 1. " + Arrays.toString(vertexId));
    }

  }

  @Override
  default boolean intersects(final BoundingBox boundingBox) {
    if (isEmpty() || boundingBox.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
      final Point point;
      if (this.getGeometryFactory().isHasCoordinateSystem()) {
        point = this.convertGeometry(geometryFactory, 2);
      } else {
        point = this;
      }
      final double x = point.getX();
      final double y = point.getY();
      return boundingBox.intersects(x, y);
    }
  }

  @Override
  default boolean intersects(final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return intersects(point);
    } else {
      final BoundingBox boundingBox = geometry.getBoundingBox();
      if (boundingBox.intersects(this)) {
        return geometry.locate(this) != Location.EXTERIOR;
      } else {
        return false;
      }
    }
  }

  @Override
  default boolean intersects(final Point point) {
    if (isEmpty()) {
      return false;
    } else {
      return equals(point);
    }
  }

  @Override
  default boolean isEmpty() {
    return getCoordinates() == null;
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof Point;
  }

  @Override
  default boolean isSimple() {
    return true;
  }

  @Override
  default boolean isValid() {
    if (!isEmpty()) {
      final double x = getX();
      final double y = getY();
      if (MathUtil.isNanOrInfinite(x, y)) {
        return false;
      }
    }
    return true;
  }

  @Override
  default Location locate(final Point point) {
    if (equals(2, point)) {
      return Location.INTERIOR;
    } else {
      return Location.EXTERIOR;
    }
  }

  @Override
  default Point move(final double... deltas) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (deltas == null || isEmpty()) {
      return this;
    } else {
      final double[] coordinates = getCoordinates();
      final int axisCount = Math.min(deltas.length, getAxisCount());
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        coordinates[axisIndex] += deltas[axisIndex];
      }
      return geometryFactory.point(coordinates);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V moveVertex(final Point newPoint, final int... vertexId) {
    if (newPoint == null || newPoint.isEmpty()) {
      return (V)this;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      return newPoint.copy(geometryFactory);
    }
  }

  @Override
  default BoundingBox newBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return new BoundingBoxDoubleGf(geometryFactory);
    } else {
      return new BoundingBoxDoubleGf(geometryFactory, this);
    }
  }

  default Point newPoint() {
    GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      geometryFactory = GeometryFactory.DEFAULT;
    }
    return geometryFactory.point(this);
  }

  default Point newPointDouble() {
    return new PointDouble(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)factory.point();
    } else {
      final double[] coordinates = getCoordinates();
      return (G)factory.point(coordinates);
    }
  }

  @Override
  default Point normalize() {
    return this;
  }

  @Override
  default Point prepare() {
    return this;
  }

  @Override
  default Point removeDuplicatePoints() {
    return this;
  }

  @Override
  default Point reverse() {
    return this;
  }

  @Override
  default PointVertex vertices() {
    return new PointVertex(this, -1);
  }
}
