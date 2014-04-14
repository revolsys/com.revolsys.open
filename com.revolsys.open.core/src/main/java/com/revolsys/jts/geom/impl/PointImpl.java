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

import java.util.Collections;

import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateFilter;
import com.revolsys.jts.geom.CoordinateSequenceComparator;
import com.revolsys.jts.geom.CoordinateSequenceFilter;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryComponentFilter;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.GeometryFilter;
import com.revolsys.jts.geom.Point;
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
public class PointImpl extends GeometryImpl implements Point {
  private static final long serialVersionUID = 4902022702746614570L;

  private static final int[] VERTEX_ID = new int[] {
    0
  };

  private double[] coordinates;

  public PointImpl(final GeometryFactory geometryFactory) {
    super(geometryFactory);
  }

  /**
   *@param  coordinates      contains the single coordinate on which to base this <code>Point</code>
   *      , or <code>null</code> to create the empty geometry.
   */
  public PointImpl(final GeometryFactory geometryFactory,
    final double... coordinates) {
    super(geometryFactory);
    final int numAxis = geometryFactory.getNumAxis();
    this.coordinates = new double[numAxis];
    for (int i = 0; i < numAxis; i++) {
      double coordinate;
      if (i < coordinates.length) {
        coordinate = coordinates[i];
      } else {
        coordinate = Double.NaN;
      }
      if (i < 2) {
        coordinate = geometryFactory.makeXyPrecise(coordinate);
      } else if (i == 2) {
        coordinate = geometryFactory.makeZPrecise(coordinate);
      }
      this.coordinates[i] = coordinate;
    }
  }

  @Override
  public double angle2d(final Coordinates other) {
    return CoordinatesUtil.angle2d(this, other);
  }

  @Override
  public void apply(final CoordinateFilter filter) {
    if (isEmpty()) {
      return;
    }
    filter.filter(getCoordinate());
  }

  @Override
  public void apply(final CoordinateSequenceFilter filter) {
    if (isEmpty()) {
      return;
    }
    filter.filter(getCoordinateSequence(), 0);
    if (filter.isGeometryChanged()) {
      geometryChanged();
    }
  }

  @Override
  public void apply(final GeometryComponentFilter filter) {
    filter.filter(this);
  }

  @Override
  public void apply(final GeometryFilter filter) {
    filter.filter(this);
  }

  /**
   * Creates and returns a full copy of this {@link Point} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public PointImpl clone() {
    final PointImpl point = (PointImpl)super.clone();
    if (coordinates != null) {
      point.coordinates = coordinates.clone();
    }
    return point;
  }

  @Override
  public Coordinates cloneCoordinates() {
    return new DoubleCoordinates(this);
  }

  @Override
  public int compareTo(final Object other) {
    if (other instanceof Geometry) {
      return super.compareTo(other);
    } else {
      return CoordinatesUtil.compareTo(this, other);
    }
  }

  @Override
  public int compareToSameClass(final Geometry other) {
    final Point point = (Point)other;
    return getCoordinate().compareTo(point.getCoordinate());
  }

  @Override
  public int compareToSameClass(final Geometry other,
    final CoordinateSequenceComparator comp) {
    final Point point = (Point)other;
    return comp.compare(this.coordinates, point.getCoordinates());
  }

  @Override
  protected Envelope computeEnvelopeInternal() {
    if (isEmpty()) {
      return new Envelope();
    } else {
      final double x = getX();
      final double y = getY();
      return new Envelope(x, y);
    }
  }

  @Override
  public Point convert(final GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (sourceGeometryFactory == geometryFactory) {
      return this;
    } else {
      return copy(geometryFactory);
    }
  }

  @Override
  public Point copy(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return geometryFactory.point();
    } else {
      geometryFactory = getNonZeroGeometryFactory(geometryFactory);
      double[] targetCoordinates;
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory.getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        targetCoordinates = coordinates;
      } else {
        final byte sourceNumAxis = getNumAxis();
        final int targetNumAxis = geometryFactory.getNumAxis();
        targetCoordinates = new double[targetNumAxis];
        coordinatesOperation.perform(sourceNumAxis, coordinates, targetNumAxis,
          targetCoordinates);
      }

      return geometryFactory.point(targetCoordinates);
    }
  }

  @Override
  public double distance(final Coordinates point) {
    return CoordinatesUtil.distance(this, point);
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
  public double distance3d(final Coordinates c) {
    final double dx = getX() - c.getX();
    final double dy = getY() - c.getY();
    final double dz = getZ() - c.getZ();
    return Math.sqrt(dx * dx + dy * dy + dz * dz);
  }

  @Override
  public boolean equals(final double... coordinates) {
    return CoordinatesUtil.equals(this, coordinates);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Point) {
      final Point point = (Point)other;
      return equalsExact(point);
    } else if (other instanceof Coordinates) {
      final Coordinates coordinates = (Coordinates)other;
      return equals2d(coordinates);
    } else {
      return super.equals(other);
    }
  }

  @Override
  public boolean equals2d(final Coordinates point) {
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
  public boolean equals2d(final Coordinates c, final double tolerance) {
    if (!NumberUtil.equalsWithTolerance(this.getX(), c.getX(), tolerance)) {
      return false;
    }
    if (!NumberUtil.equalsWithTolerance(this.getY(), c.getY(), tolerance)) {
      return false;
    }
    return true;
  }

  @Override
  public boolean equals3d(final Coordinates point) {
    return CoordinatesUtil.equals3d(this, point);
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    if (isEmpty() && other.isEmpty()) {
      return true;
    }
    if (isEmpty() != other.isEmpty()) {
      return false;
    }
    return equal(((Point)other).getCoordinate(), this.getCoordinate(),
      tolerance);
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
    return getGeometryFactory().createEmptyGeometryCollection();
  }

  @Override
  public int getBoundaryDimension() {
    return Dimension.FALSE;
  }

  @Override
  public Coordinates getCoordinate() {
    if (isEmpty()) {
      return null;
    } else {
      final double x = getX();
      final double y = getY();
      final double z = getZ();
      return new Coordinate(x, y, z);
    }
  }

  @Override
  public double getCoordinate(final int axisIndex) {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      final byte numAxis = getNumAxis();
      if (axisIndex >= 0 && axisIndex < numAxis) {
        return coordinates[axisIndex];
      } else {
        return Double.NaN;
      }
    }
  }

  @Override
  public Coordinates[] getCoordinateArray() {
    if (isEmpty()) {
      return new Coordinates[0];
    } else {
      return new Coordinates[] {
        getCoordinate()
      };
    }
  }

  @Override
  public double[] getCoordinates() {
    return CoordinatesUtil.getCoordinates(this);
  }

  @Override
  public CoordinatesList getCoordinateSequence() {
    final byte numAxis = getNumAxis();
    return new DoubleCoordinatesList(numAxis, coordinates);
  }

  @Override
  public DataType getDataType() {
    return DataTypes.POINT;
  }

  @Override
  public int getDimension() {
    return 0;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V getGeometry() {
    return (V)this;
  }

  @Override
  public double getM() {
    return getCoordinate(3);
  }

  @Override
  public int getNumPoints() {
    return isEmpty() ? 0 : 1;
  }

  @Override
  public int getPartIndex() {
    return -1;
  }

  @Override
  public int getRingIndex() {
    return -1;
  }

  @Override
  public long getTime() {
    return (long)getM();
  }

  @Override
  public double getValue(final int index) {
    return getCoordinate(index);
  }

  @Override
  public Vertex getVertex(final int... vertexId) {
    if (isEmpty()) {
      return null;
    } else {
      if (vertexId.length == 1) {
        if (vertexId[0] == 0) {
          return this;
        }
      }
      return null;
    }
  }

  @Override
  public int[] getVertexId() {
    return VERTEX_ID;
  }

  @Override
  public int getVertexIndex() {
    return 0;
  }

  @Override
  public double getX() {
    if (isEmpty()) {
      throw new IllegalStateException("getX called on empty Point");
    } else {
      return getCoordinate(0);
    }
  }

  @Override
  public double getY() {
    if (isEmpty()) {
      throw new IllegalStateException("getY called on empty Point");
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
    return coordinates == null;
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
  public Point normalize() {
    return this;
  }

  @Override
  public Geometry reverse() {
    return clone();
  }

  @Override
  public void setCoordinate(final Coordinates other) {
    throw new IllegalArgumentException("Geometries cannot be modified");
  }

  @Override
  public void setM(final double m) {
    throw new IllegalArgumentException("Geometries cannot be modified");
  }

  @Override
  public void setTime(final long time) {
    throw new IllegalArgumentException("Geometries cannot be modified");
  }

  @Override
  public void setValue(final int index, final double value) {
    throw new IllegalArgumentException("Geometries cannot be modified");
  }

  @Override
  public void setX(final double x) {
    throw new IllegalArgumentException("Geometries cannot be modified");
  }

  @Override
  public void setY(final double y) {
    throw new IllegalArgumentException("Geometries cannot be modified");
  }

  @Override
  public void setZ(final double z) {
    throw new IllegalArgumentException("Geometries cannot be modified");
  }

  @Override
  public Point toPoint() {
    return this;
  }

  @Override
  public Iterable<Vertex> vertices() {
    return Collections.<Vertex> singletonList(this);
  }
}
