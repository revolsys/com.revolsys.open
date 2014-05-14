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
package com.revolsys.jts.geom.impl;

import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.data.equals.NumberEquals;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.CoordinateSequenceComparator;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.PointVertex;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.util.NumberUtil;
import com.revolsys.util.MathUtil;

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
public abstract class AbstractPoint extends AbstractGeometry implements Point {
  private static final long serialVersionUID = 4902022702746614570L;

  @Override
  public double angle2d(final Point other) {
    return CoordinatesUtil.angle2d(this, other);
  }

  /**
   * Creates and returns a full copy of this {@link Point} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public AbstractPoint clone() {
    return (AbstractPoint)super.clone();
  }

  @Override
  public Point cloneCoordinates() {
    return new PointDouble(this);
  }

  @Override
  public int compareTo(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
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
        return CoordinatesUtil.compareToXY(this, point);
      }
    } else if (other instanceof Geometry) {
      return super.compareTo(other);
    } else {
      return -1;
    }
  }

  @Override
  public int compareToSameClass(final Geometry other) {
    final Point point = (Point)other;
    return getPoint().compareTo(point.getPoint());
  }

  @Override
  public int compareToSameClass(final Geometry other,
    final CoordinateSequenceComparator comp) {
    final Point point = (Point)other;
    return comp.compare(getCoordinatesList(), point.getCoordinatesList());
  }

  @Override
  protected BoundingBox computeBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return new Envelope(geometryFactory);
    } else {
      return new Envelope(geometryFactory, this);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V copy(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return (V)this.clone();
    } else if (isEmpty()) {
      return (V)geometryFactory.point();
    } else {
      geometryFactory = getNonZeroGeometryFactory(geometryFactory);
      double[] targetCoordinates;
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory.getCoordinatesOperation(geometryFactory);
      final double[] coordinates = getCoordinates();
      if (coordinatesOperation == null) {
        targetCoordinates = coordinates;
      } else {
        final int sourceAxisCount = getAxisCount();
        final int targetAxisCount = geometryFactory.getAxisCount();
        targetCoordinates = new double[targetAxisCount];
        coordinatesOperation.perform(sourceAxisCount, coordinates,
          targetAxisCount, targetCoordinates);
      }

      return (V)geometryFactory.point(targetCoordinates);
    }
  }

  @Override
  public double distance(final Point point) {
    return CoordinatesUtil.distance(this, point);
  }

  /**
   * Computes the 3-dimensional Euclidean distance to another location.
   * 
   * @param c a coordinate
   * @return the 3-dimensional Euclidean distance between the locations
   */
  @Override
  public double distance3d(final Point c) {
    final double dx = getX() - c.getX();
    final double dy = getY() - c.getY();
    final double dz = getZ() - c.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  @Override
  protected boolean doEqualsExact(final Geometry geometry) {
    final Point point = (Point)geometry;
    for (int i = 0; i < getAxisCount(); i++) {
      final double value = getCoordinate(i);
      final double otherValue = point.getCoordinate(i);
      if (!NumberEquals.equal(value, otherValue)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(final double... coordinates) {
    return CoordinatesUtil.equals(this, coordinates);
  }

  @Override
  public boolean equals(final int axisCount, final Point point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = getCoordinate(axisIndex);
      final double value2 = point.getCoordinate(axisIndex);
      if (!NumberEquals.equal(value, value2)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return equalsExact2d(point);
    } else if (other instanceof Point) {
      final Point coordinates = (Point)other;
      return equals2d(coordinates);
    } else {
      return super.equals(other);
    }
  }

  @Override
  public boolean equals(final Point point) {
    return equals2d(point);
  }

  @Override
  public boolean equals2d(final Point point) {
    return CoordinatesUtil.equals2d(this, point);
  }

  /**
   * Tests if another coordinate has the same values for the X and Y ordinates.
   * The Z ordinate is ignored.
   *
   *@param other a <code>Coordinate</code> with which to do the 2D comparison.
   *@return true if <code>other</code> is a <code>Coordinate</code>
   *      with the same values for X and Y.
   */
  @Override
  public boolean equals2d(final Point c, final double tolerance) {
    if (!NumberUtil.equalsWithTolerance(this.getX(), c.getX(), tolerance)) {
      return false;
    }
    if (!NumberUtil.equalsWithTolerance(this.getY(), c.getY(), tolerance)) {
      return false;
    }
    return true;
  }

  @Override
  public boolean equals3d(final Point point) {
    return CoordinatesUtil.equals3d(this, point);
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
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

  @Override
  public boolean equalsExact3d(final Geometry geometry) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return equals3d(point);
    }
    return false;
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
  public Geometry getBoundary() {
    return getGeometryFactory().geometryCollection();
  }

  @Override
  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  @Override
  public double[] getCoordinates() {
    return CoordinatesUtil.getCoordinates(this);
  }

  @Override
  public PointList getCoordinatesList() {
    final int axisCount = getAxisCount();
    final double[] coordinates = getCoordinates();
    return new DoubleCoordinatesList(axisCount, coordinates);
  }

  @Override
  public DataType getDataType() {
    return DataTypes.POINT;
  }

  @Override
  public int getDimension() {
    return 0;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return GeometryFactory.getFactory();
  }

  @Override
  public double getM() {
    return getCoordinate(3);
  }

  @Override
  public Point getPoint() {
    return this;
  }

  @Override
  public Segment getSegment(final int... segmentId) {
    return null;
  }

  @Override
  public long getTime() {
    return (long)getM();
  }

  @Override
  public Vertex getVertex(final int... vertexId) {
    if (isEmpty()) {
      return null;
    } else {
      if (vertexId.length == 1) {
        if (vertexId[0] == 0) {
          return new PointVertex(this, vertexId);
        }
      }
      return null;
    }
  }

  @Override
  public int getVertexCount() {
    return isEmpty() ? 0 : 1;
  }

  @Override
  public double getX() {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      return getCoordinate(0);
    }
  }

  @Override
  public double getY() {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      return getCoordinate(1);
    }
  }

  @Override
  public double getZ() {
    return getCoordinate(2);
  }

  @Override
  public int hashCode() {
    return CoordinatesUtil.hashCode(this);
  }

  @Override
  public boolean isEmpty() {
    return getCoordinates() == null;
  }

  @Override
  protected boolean isEquivalentClass(final Geometry other) {
    return other instanceof Point;
  }

  @Override
  public boolean isSimple() {
    return true;
  }

  @Override
  public boolean isValid() {
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
  public Point move(final double... deltas) {
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
  public Point normalize() {
    return this;
  }

  @Override
  public Geometry reverse() {
    return clone();
  }

  @Override
  public Reader<Segment> segments() {
    return new IteratorReader<>();
  }

  @Override
  public Reader<Vertex> vertices() {
    final PointVertex vertex = new PointVertex(this, -1);
    return vertex.reader();
  }
}
