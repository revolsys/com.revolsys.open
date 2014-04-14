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

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.jts.algorithm.CGAlgorithms;
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
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.vertex.AbstractVertex;
import com.revolsys.jts.geom.vertex.LineStringVertex;
import com.revolsys.jts.geom.vertex.LineStringVertexIterable;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.operation.BoundaryOp;

/**
 *  Models an OGC-style <code>LineString</code>.
 *  A LineString consists of a sequence of two or more vertices,
 *  along with all points along the linearly-interpolated curves
 *  (line segments) between each 
 *  pair of consecutive vertices.
 *  Consecutive vertices may be equal.
 *  The line segments in the line may intersect each other (in other words, 
 *  the linestring may "curl back" in itself and self-intersect.
 *  Linestrings with exactly two identical points are invalid. 
 *  <p> 
 * A linestring must have either 0 or 2 or more points.  
 * If these conditions are not met, the constructors throw 
 * an {@link IllegalArgumentException}
 *
 *@version 1.7
 */
public class LineStringImpl extends GeometryImpl implements LineString {

  private static final long serialVersionUID = 3110669828065365560L;

  /**
   *  The points of this <code>LineString</code>.
   */
  private CoordinatesList points;

  /**
   * Constructs a <code>LineString</code> with the given points.
   *  
   *@param  points the points of the linestring, or <code>null</code>
   *      to create the empty geometry. 
   * @throws IllegalArgumentException if too few points are provided
   */
  public LineStringImpl(final CoordinatesList points,
    final GeometryFactory factory) {
    super(factory);
    init(points);
  }

  @Override
  public void apply(final CoordinateFilter filter) {
    for (int i = 0; i < points.size(); i++) {
      filter.filter(points.getCoordinate(i));
    }
  }

  @Override
  public void apply(final CoordinateSequenceFilter filter) {
    if (points.size() == 0) {
      return;
    }
    for (int i = 0; i < points.size(); i++) {
      filter.filter(points, i);
      if (filter.isDone()) {
        break;
      }
    }
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
   * Creates and returns a full copy of this {@link LineString} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */
  @Override
  public LineStringImpl clone() {
    final LineStringImpl line = (LineStringImpl)super.clone();
    line.points = points.clone();
    return line;
  }

  @Override
  public int compareToSameClass(final Geometry o) {
    final LineString line = (LineString)o;
    // MD - optimized implementation
    int i = 0;
    int j = 0;
    while (i < points.size() && j < line.getNumPoints()) {
      final int comparison = getCoordinateN(i).compareTo(line.getCoordinateN(j));
      if (comparison != 0) {
        return comparison;
      }
      i++;
      j++;
    }
    if (i < points.size()) {
      return 1;
    }
    if (j < line.getNumPoints()) {
      return -1;
    }
    return 0;
  }

  @Override
  public int compareToSameClass(final Geometry o,
    final CoordinateSequenceComparator comp) {
    final LineString line = (LineString)o;
    return comp.compare(this.points, line.getPointList());
  }

  @Override
  protected Envelope computeEnvelopeInternal() {
    if (isEmpty()) {
      return new Envelope();
    }
    return points.expandEnvelope(new Envelope());
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    final LineString otherLineString = (LineString)other;
    if (getNumPoints() != otherLineString.getNumPoints()) {
      return false;
    }
    for (int i = 0; i < points.size(); i++) {
      final Coordinates point = getCoordinateN(i);
      final Coordinates otherPoint = otherLineString.getCoordinateN(i);
      if (!equal(point, otherPoint, tolerance)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the boundary of this geometry.
   * The boundary of a lineal geometry is always a zero-dimensional geometry (which may be empty).
   *
   * @return the boundary geometry
   * @see Geometry#getBoundary
   */
  @Override
  public Geometry getBoundary() {
    return (new BoundaryOp(this)).getBoundary();
  }

  @Override
  public int getBoundaryDimension() {
    if (isClosed()) {
      return Dimension.FALSE;
    }
    return 0;
  }

  @Override
  public Coordinates getCoordinate() {
    if (isEmpty()) {
      return null;
    }
    return points.getCoordinate(0);
  }

  @Override
  public double getCoordinate(int vertexIndex, final int axisIndex) {
    final int numPoints = points.size();
    if (vertexIndex < numPoints) {
      while (vertexIndex < 0) {
        vertexIndex += numPoints;
      }
      return points.getOrdinate(vertexIndex, axisIndex);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public Coordinates[] getCoordinateArray() {
    return points.toCoordinateArray();
  }

  @Override
  public Coordinates getCoordinateN(final int n) {
    return points.getCoordinate(n);
  }

  @Override
  public CoordinatesList getCoordinatesList() {
    return points;
  }

  @Override
  public DataType getDataType() {
    return DataTypes.LINE_STRING;
  }

  @Override
  public int getDimension() {
    return 1;
  }

  @Override
  public Point getEndPoint() {
    if (isEmpty()) {
      return null;
    }
    return getPointN(getNumPoints() - 1);
  }

  /**
   *  Returns the length of this <code>LineString</code>
   *
   *@return the length of the linestring
   */
  @Override
  public double getLength() {
    return CGAlgorithms.length(points);
  }

  @Override
  public int getNumPoints() {
    return points.size();
  }

  @Override
  public CoordinatesList getPointList() {
    return CoordinatesListUtil.get(this);
  }

  @Override
  public Point getPointN(final int n) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Coordinates coordinate = points.getCoordinate(n);
    return geometryFactory.point(coordinate);
  }

  @Override
  public Point getStartPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return getPointN(0);
    }
  }

  @Override
  public AbstractVertex getVertex(final int... vertexId) {
    if (vertexId.length == 1) {
      final int vertexIndex = vertexId[0];
      return getVertex(vertexIndex);
    }
    return null;
  }

  @Override
  public AbstractVertex getVertex(int vertexIndex) {
    final int vertexCount = points.size();
    if (vertexIndex < vertexCount) {
      while (vertexIndex < 0) {
        vertexIndex += vertexCount;
      }
      return new LineStringVertex(this, vertexIndex);
    }
    return null;
  }

  @Override
  public int getVertexCount() {
    return points.size();
  }

  private void init(CoordinatesList points) {
    if (points == null) {
      points = getGeometryFactory().getCoordinateSequenceFactory().create(
        new Coordinates[] {});
    }
    if (points.size() == 1) {
      throw new IllegalArgumentException(
        "Invalid number of points in LineString (found " + points.size()
          + " - must be 0 or >= 2)");
    }
    this.points = points;
  }

  @Override
  public boolean isClosed() {
    if (isEmpty()) {
      return false;
    } else {
      final double x1 = getCoordinate(0, 0);
      final double xn = getCoordinate(-1, 0);
      if (x1 == xn) {
        final double y1 = getCoordinate(0, 1);
        final double yn = getCoordinate(-1, 1);
        if (y1 == yn) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return points.size() == 0;
  }

  @Override
  protected boolean isEquivalentClass(final Geometry other) {
    return other instanceof LineString;
  }

  @Override
  public boolean isRing() {
    return isClosed() && isSimple();
  }

  /**
   * Normalizes a LineString.  A normalized linestring
   * has the first point which is not equal to it's reflected point
   * less than the reflected point.
   */
  @Override
  public LineString normalize() {
    for (int i = 0; i < points.size() / 2; i++) {
      final int j = points.size() - 1 - i;
      // skip equal points on both ends
      if (!points.equal(i, points, j, 2)) {
        if (points.getCoordinate(i).compareTo(points.getCoordinate(j)) > 0) {
          return reverse();
        }
        return this;
      }
    }
    return this;
  }

  @Override
  public Iterable<Coordinates> points() {
    return getPointList();
  }

  /**
   * Creates a {@link LineString} whose coordinates are in the reverse
   * order of this objects
   *
   * @return a {@link LineString} with coordinates in the reverse order
   */
  @Override
  public LineString reverse() {
    final CoordinatesList reversePoints = this.points.reverse();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LineString reverseLine = geometryFactory.lineString(reversePoints);
    return reverseLine;
  }

  @Override
  public Iterable<Vertex> vertices() {
    return new LineStringVertexIterable(this);
  }

}
