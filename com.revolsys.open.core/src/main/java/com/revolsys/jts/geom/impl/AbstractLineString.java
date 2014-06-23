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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.cs.projection.CoordinatesOperation;
import com.revolsys.gis.data.io.IteratorReader;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.jts.GeometryProperties;
import com.revolsys.gis.jts.LineStringUtil;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.data.equals.NumberEquals;
import com.revolsys.io.Reader;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryComponent;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.prep.PreparedLineString;
import com.revolsys.jts.geom.segment.LineStringSegment;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.AbstractVertex;
import com.revolsys.jts.geom.vertex.LineStringVertex;
import com.revolsys.jts.geom.vertex.Vertex;
import com.revolsys.jts.operation.BoundaryOp;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.MathUtil;

/**
 * Models an OGC-style <code>LineString</code>. A LineString consists of a
 * sequence of two or more vertices, along with all points along the
 * linearly-interpolated curves (line segments) between each pair of consecutive
 * vertices. Consecutive vertices may be equal. The line segments in the line
 * may intersect each other (in other words, the linestring may "curl back" in
 * itself and self-intersect. Linestrings with exactly two identical points are
 * invalid.
 * <p>
 * A linestring must have either 0 or 2 or more points. If these conditions are
 * not met, the constructors throw an {@link IllegalArgumentException}
 * 
 * @version 1.7
 */
public abstract class AbstractLineString extends AbstractGeometry implements
  LineString {

  private static final long serialVersionUID = 3110669828065365560L;

  /**
   * Creates and returns a full copy of this {@link LineString} object.
   * (including all coordinates contained by it).
   * 
   * @return a clone of this instance
   */
  @Override
  public AbstractLineString clone() {
    final AbstractLineString line = (AbstractLineString)super.clone();
    return line;
  }

  @Override
  public int compareToSameClass(final Geometry geometry) {
    final LineString line2 = (LineString)geometry;
    final Iterator<Vertex> iterator1 = vertices().iterator();
    final Iterator<Vertex> iterator2 = line2.vertices().iterator();
    while (iterator1.hasNext() && iterator2.hasNext()) {
      final Point vertex1 = iterator1.next();
      final Point vertex2 = iterator2.next();
      final int comparison = vertex1.compareTo(vertex2);
      if (comparison != 0) {
        return comparison;
      }
    }
    if (iterator1.hasNext()) {
      return 1;
    } else if (iterator2.hasNext()) {
      return -1;
    } else {
      return 0;
    }
  }

  @Override
  protected BoundingBox computeBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return new BoundingBoxDoubleGf(geometryFactory);
    } else {
      return new BoundingBoxDoubleGf(geometryFactory, vertices());
    }
  }

  protected double[] convertCoordinates(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    final double[] coordinates = getCoordinates();
    if (isEmpty()) {
      return coordinates;
    } else {
      geometryFactory = getNonZeroGeometryFactory(geometryFactory);
      double[] targetCoordinates;
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory.getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        return coordinates;
      } else {
        final int sourceAxisCount = getAxisCount();
        targetCoordinates = new double[sourceAxisCount * getVertexCount()];
        coordinatesOperation.perform(sourceAxisCount, coordinates,
          sourceAxisCount, targetCoordinates);
        return targetCoordinates;
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return (V)this.clone();
    } else if (isEmpty()) {
      return (V)geometryFactory.lineString();
    } else {
      final double[] coordinates = convertCoordinates(geometryFactory);
      final int axisCount = getAxisCount();
      return (V)geometryFactory.lineString(axisCount, coordinates);
    }
  }

  @Override
  public double distance(final int index, final Point point) {
    if (index < getVertexCount()) {
      final double x1 = getX(index);
      final double y1 = getY(index);
      final double x2 = point.getX();
      final double y2 = point.getY();
      return MathUtil.distance(x1, y1, x2, y2);
    } else {
      return Double.NaN;
    }
  }

  @Override
  public boolean doEquals(final int axisCount, final Geometry geometry) {
    final LineString line = (LineString)geometry;
    final int vertexCount = getVertexCount();
    final int vertexCount2 = line.getVertexCount();
    if (vertexCount == vertexCount2) {
      for (int i = 0; i < vertexCount2; i++) {
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          final double value1 = getCoordinate(i, axisIndex);
          final double value2 = line.getCoordinate(i, axisIndex);
          if (!NumberEquals.equal(value1, value2)) {
            return false;
          }
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(final int axisCount, final int vertexIndex,
    final Point point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = getCoordinate(vertexIndex, axisIndex);
      final double value2 = point.getCoordinate(axisIndex);
      if (!NumberEquals.equal(value, value2)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equalsExact(final Geometry other, final double tolerance) {
    if (!isEquivalentClass(other)) {
      return false;
    }
    final LineString otherLineString = (LineString)other;
    if (getVertexCount() != otherLineString.getVertexCount()) {
      return false;
    }
    for (int i = 0; i < getVertexCount(); i++) {
      final Point point = getPoint(i);
      final Point otherPoint = otherLineString.getPoint(i);
      if (!equal(point, otherPoint, tolerance)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equalsVertex(final int vertexIndex,
    final double... coordinates) {
    for (int axisIndex = 0; axisIndex < coordinates.length; axisIndex++) {
      final double coordinate = coordinates[axisIndex];
      final double matchCoordinate = getCoordinate(vertexIndex, axisIndex);
      if (!NumberEquals.equal(coordinate, matchCoordinate)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int vertexIndex1,
    final int vertexIndex2) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate1 = getCoordinate(vertexIndex1, axisIndex);
      final double coordinate2 = getCoordinate(vertexIndex2, axisIndex);
      if (!NumberEquals.equal(coordinate1, coordinate2)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int vertexIndex,
    final LineString line2, final int vertexIndex2) {
    if (line2.isEmpty()) {
      return false;
    } else {
      final Vertex vertex2 = line2.getVertex(vertexIndex2);
      return equalsVertex(axisCount, vertexIndex, vertex2);
    }
  }

  @Override
  public boolean equalsVertex(final int axisCount, final int vertexIndex,
    Point point) {
    point = point.convert(getGeometryFactory(), axisCount);
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double coordinate = point.getCoordinate(axisIndex);
      final double matchCoordinate = getCoordinate(vertexIndex, axisIndex);
      if (!NumberEquals.equal(coordinate, matchCoordinate)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equalsVertex(final int vertexIndex, Point point) {
    point = point.convert(getGeometryFactory());
    for (int axisIndex = 0; axisIndex < point.getAxisCount(); axisIndex++) {
      final double coordinate = point.getCoordinate(axisIndex);
      final double matchCoordinate = getCoordinate(vertexIndex, axisIndex);
      if (!NumberEquals.equal(coordinate, matchCoordinate)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Gets the boundary of this geometry. The boundary of a lineal geometry is
   * always a zero-dimensional geometry (which may be empty).
   * 
   * @return the boundary geometry
   * @see Geometry#getBoundary
   */
  @Override
  public Geometry getBoundary() {
    return new BoundaryOp(this).getBoundary();
  }

  @Override
  public int getBoundaryDimension() {
    if (isClosed()) {
      return Dimension.FALSE;
    }
    return 0;
  }

  @Override
  public abstract double[] getCoordinates();

  @Override
  public LineString getCoordinatesList() {
    return this;
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
    return getPoint(getVertexCount() - 1);
  }

  /**
   * Returns the length of this <code>LineString</code>
   * 
   * @return the length of the linestring
   */
  @Override
  public double getLength() {
    final int vertexCount = getVertexCount();
    if (vertexCount <= 1) {
      return 0.0;
    } else {
      double len = 0.0;
      double x0 = getX(0);
      double y0 = getY(0);
      for (int i = 1; i < vertexCount; i++) {
        final double x1 = getX(i);
        final double y1 = getY(i);
        final double dx = x1 - x0;
        final double dy = y1 - y0;
        len += Math.sqrt(dx * dx + dy * dy);
        x0 = x1;
        y0 = y1;
      }
      return len;
    }
  }

  @Override
  public double getM(final int vertexIndex) {
    return getCoordinate(vertexIndex, 3);
  }

  @Override
  public Point getPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return getPoint(0);
    }
  }

  @Override
  public final Point getPoint(int vertexIndex) {
    if (isEmpty()) {
      return null;
    } else {
      while (vertexIndex < 0) {
        vertexIndex += getVertexCount();
      }
      if (vertexIndex > getVertexCount()) {
        return null;
      } else {
        final int axisCount = getAxisCount();
        final double[] coordinates = new double[axisCount];
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          coordinates[axisIndex] = getCoordinate(vertexIndex, axisIndex);
        }
        final GeometryFactory geometryFactory = getGeometryFactory();
        return geometryFactory.point(coordinates);
      }
    }
  }

  @Override
  public LineStringSegment getSegment(final int... segmentId) {
    if (segmentId.length == 1) {
      int segmentIndex = segmentId[0];
      final int vertexCount = getSegmentCount();
      if (segmentIndex < vertexCount) {
        while (segmentIndex < 0) {
          segmentIndex += vertexCount;
        }
        return new LineStringSegment(this, segmentIndex);
      }
    }
    return null;
  }

  @Override
  public int getSegmentCount() {
    if (isEmpty()) {
      return 0;
    } else {
      return getVertexCount() - 1;
    }
  }

  @Override
  public Point getStartPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return getPoint(0);
    }
  }

  @Override
  public AbstractVertex getVertex(final int... vertexId) {
    if (vertexId.length == 1) {
      int vertexIndex = vertexId[0];
      final int vertexCount = getVertexCount();
      if (vertexIndex < vertexCount) {
        while (vertexIndex < 0) {
          vertexIndex += vertexCount;
        }
        return new LineStringVertex(this, vertexIndex);
      }
    }
    return null;
  }

  @Override
  public double getX(final int vertexIndex) {
    return getCoordinate(vertexIndex, 0);
  }

  @Override
  public double getY(final int vertexIndex) {
    return getCoordinate(vertexIndex, 1);
  }

  @Override
  public double getZ(final int vertexIndex) {
    return getCoordinate(vertexIndex, 2);
  }

  @Override
  public boolean hasVertex(final Point point) {
    final int vertexCount = getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      if (equalsVertex(2, vertexIndex, point)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean intersects(final BoundingBox boundingBox) {
    if (isEmpty() || boundingBox.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = boundingBox.getGeometryFactory()
        .convertAxisCount(2);
      double previousX = Double.NaN;
      double previousY = Double.NaN;

      final double[] coordinates = new double[2];
      for (final Vertex vertex : vertices()) {
        vertex.copyCoordinates(geometryFactory, coordinates);
        final double x = coordinates[0];
        final double y = coordinates[1];
        if (!Double.isNaN(previousX)) {
          if (boundingBox.intersects(previousX, previousY, x, y)) {
            return true;
          }
        }
        previousX = x;
        previousY = y;
      }
      return false;
    }
  }

  @Override
  public boolean isClockwise() {
    return !isCounterClockwise();
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
  public boolean isCounterClockwise() {
    final int pointCount = getVertexCount() - 1;

    // find highest point
    double hiPtX = getX(0);
    double hiPtY = getY(0);
    int hiIndex = 0;
    for (int i = 1; i <= pointCount; i++) {
      final double x = getX(i);
      final double y = getY(i);
      if (y > hiPtY) {
        hiPtX = x;
        hiPtY = y;
        hiIndex = i;
      }
    }

    // find distinct point before highest point
    int iPrev = hiIndex;
    do {
      iPrev = iPrev - 1;
      if (iPrev < 0) {
        iPrev = pointCount;
      }
    } while (equalsVertex(iPrev, hiPtX, hiPtY) && iPrev != hiIndex);

    // find distinct point after highest point
    int iNext = hiIndex;
    do {
      iNext = (iNext + 1) % pointCount;
    } while (equalsVertex(iNext, hiPtX, hiPtY) && iNext != hiIndex);

    /**
     * This check catches cases where the ring contains an A-B-A configuration
     * of points. This can happen if the ring does not contain 3 distinct points
     * (including the case where the input array has fewer than 4 elements), or
     * it contains coincident line segments.
     */
    if (equalsVertex(iPrev, hiPtX, hiPtY) || equalsVertex(iNext, hiPtX, hiPtY)
      || equalsVertex(2, iPrev, iNext)) {
      return false;
    }

    final int disc = orientationIndex(iPrev, hiIndex, iNext);

    /**
     * If disc is exactly 0, lines are collinear. There are two possible cases:
     * (1) the lines lie along the x axis in opposite directions (2) the lines
     * lie on top of one another (1) is handled by checking if next is left of
     * prev ==> CCW (2) will never happen if the ring is valid, so don't check
     * for it (Might want to assert this)
     */
    boolean counterClockwise = false;
    if (disc == 0) {
      // poly is CCW if prev x is right of next x
      final double prevX = getX(iPrev);
      final double nextX = getX(iNext);
      counterClockwise = prevX > nextX;
    } else {
      // if area is positive, points are ordered CCW
      counterClockwise = disc > 0;
    }
    return counterClockwise;
  }

  @Override
  protected boolean isEquivalentClass(final Geometry other) {
    return other instanceof LineString;
  }

  @Override
  public boolean isRing() {
    return isClosed() && isSimple();
  }

  @Override
  public LineString merge(final LineString line2) {
    final int axisCount = Math.max(getAxisCount(), line2.getAxisCount());
    final int vertexCount1 = getVertexCount();
    final int vertexCount2 = line2.getVertexCount();
    final int vertexCount = vertexCount1 + vertexCount2 - 1;
    final double[] coordinates = new double[vertexCount * axisCount];

    int newVertexCount = 0;
    final Point line1From = getVertex(0);
    final Point line1To = getVertex(-1);
    final Point line2From = line2.getVertex(0);
    final Point line2To = line2.getVertex(-1);
    if (line1From.equals(2, line2To)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, line2, 0,
        coordinates, 0, vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1,
        coordinates, newVertexCount, vertexCount1 - 1);
    } else if (line2From.equals(2, line1To)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0,
        coordinates, 0, vertexCount1);
      newVertexCount = CoordinatesListUtil.append(axisCount, line2, 1,
        coordinates, newVertexCount, vertexCount2 - 1);
    } else if (line1From.equals(2, line2From)) {
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line2, 0,
        coordinates, 0, vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1,
        coordinates, newVertexCount, vertexCount1 - 1);
    } else if (line1To.equals(2, line2To)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0,
        coordinates, newVertexCount, vertexCount1);
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line2, 1,
        coordinates, newVertexCount, vertexCount2 - 1);
    } else {
      throw new IllegalArgumentException("lines don't touch\n" + this + "\n"
        + line2);

    }
    final GeometryFactory factory = getGeometryFactory();
    final LineString newLine = factory.lineString(axisCount, newVertexCount,
      coordinates);
    GeometryProperties.copyUserData(this, newLine);
    return newLine;
  }

  @Override
  public LineString merge(final Point point, final LineString line2) {
    final int axisCount = Math.max(getAxisCount(), line2.getAxisCount());
    final int vertexCount1 = getVertexCount();
    final int vertexCount2 = line2.getVertexCount();
    final int vertexCount = vertexCount1 + vertexCount2 - 1;
    final double[] coordinates = new double[vertexCount * axisCount];

    int newVertexCount = 0;
    final Point line1From = getVertex(0);
    final Point line1To = getVertex(-1);
    final Point line2From = line2.getVertex(0);
    final Point line2To = line2.getVertex(-1);
    if (line1From.equals(2, line2To) && line1From.equals(2, point)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, line2, 0,
        coordinates, 0, vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1,
        coordinates, newVertexCount, vertexCount1 - 1);
    } else if (line2From.equals(2, line1To) && line2From.equals(2, point)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0,
        coordinates, 0, vertexCount1);
      newVertexCount = CoordinatesListUtil.append(axisCount, line2, 1,
        coordinates, newVertexCount, vertexCount2 - 1);
    } else if (line1From.equals(2, line2From) && line1From.equals(2, point)) {
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line2, 0,
        coordinates, 0, vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1,
        coordinates, newVertexCount, vertexCount1 - 1);
    } else if (line1To.equals(2, line2To) && line1To.equals(2, point)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0,
        coordinates, newVertexCount, vertexCount1);
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line2, 1,
        coordinates, newVertexCount, vertexCount2 - 1);
    } else {
      throw new IllegalArgumentException("lines don't touch\n" + this + "\n"
        + line2);

    }
    final GeometryFactory factory = getGeometryFactory();
    final LineString newLine = factory.lineString(axisCount, newVertexCount,
      coordinates);
    GeometryProperties.copyUserData(this, newLine);
    return newLine;
  }

  @Override
  public LineString move(final double... deltas) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (deltas == null || isEmpty()) {
      return this;
    } else {
      final double[] coordinates = moveCoordinates(deltas);
      final int axisCount = getAxisCount();
      return geometryFactory.lineString(axisCount, coordinates);
    }
  }

  protected double[] moveCoordinates(final double... deltas) {
    final double[] coordinates = getCoordinates();
    final int vertexCount = getVertexCount();
    final int axisCount = getAxisCount();
    final int deltaCount = Math.min(deltas.length, getAxisCount());
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < deltaCount; axisIndex++) {
        coordinates[vertexIndex * axisCount + axisIndex] += deltas[axisIndex];
      }
    }
    return coordinates;
  }

  /**
   * Normalizes a LineString. A normalized linestring has the first point which
   * is not equal to it's reflected point less than the reflected point.
   */
  @Override
  public LineString normalize() {
    final int vertexCount = getVertexCount();
    for (int i = 0; i < vertexCount / 2; i++) {
      final int j = vertexCount - 1 - i;
      final Vertex point1 = getVertex(i);
      final Vertex point2 = getVertex(j);
      // skip equal points on both ends
      if (!point1.equals(2, point2)) {
        if (point1.compareTo(point2) > 0) {
          return reverse();
        }
        return this;
      }
    }
    return this;
  }

  public int orientationIndex(final int index1, final int index2,
    final int index) {
    final double x1 = getX(index1);
    final double y1 = getY(index1);
    final double x2 = getX(index2);
    final double y2 = getY(index2);
    final double x = getX(index);
    final double y = getY(index);
    return CoordinatesListUtil.orientationIndex(x1, y1, x2, y2, x, y);
  }

  @Override
  public Iterable<Point> points() {
    final List<Point> points = new ArrayList<>();
    for (int i = 0; i < getVertexCount(); i++) {
      final Point point = getPoint(i);
      points.add(point);
    }
    return points;
  }

  @Override
  @Deprecated
  public LineString prepare() {
    return new PreparedLineString(this);
  }

  /**
   * Creates a {@link LineString} whose coordinates are in the reverse order of
   * this objects
   * 
   * @return a {@link LineString} with coordinates in the reverse order
   */
  @Override
  public LineString reverse() {
    final int vertexCount = getVertexCount();
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final int coordinateIndex = (vertexCount - 1 - vertexIndex) * axisCount
          + axisIndex;
        coordinates[coordinateIndex] = getCoordinate(vertexIndex, axisIndex);
      }
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LineString reverseLine = geometryFactory.lineString(axisCount,
      coordinates);
    GeometryProperties.copyUserData(this, reverseLine);
    return reverseLine;
  }

  @Override
  public Reader<Segment> segments() {
    final LineStringSegment iterator = new LineStringSegment(this, -1);
    return new IteratorReader<Segment>(iterator);
  }

  @Override
  public List<LineString> split(Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    point = point.convert(getGeometryFactory());
    final Map<GeometryComponent, Double> result = LineStringUtil.findClosestGeometryComponent(
      this, point);
    if (result.isEmpty()) {
      return Collections.<LineString> singletonList(this);
    } else {
      final int vertexCount = getVertexCount();
      final GeometryComponent geometryComponent = CollectionUtil.get(
        result.keySet(), 0);
      final double distance = CollectionUtil.get(result.values(), 0);
      if (geometryComponent instanceof Vertex) {
        final Vertex vertex = (Vertex)geometryComponent;
        final int vertexIndex = vertex.getVertexIndex();
        if (distance == 0) {
          if (vertexIndex <= 0 || vertexIndex >= vertexCount - 1) {
            return Collections.<LineString> singletonList(this);
          } else {
            final LineString line1 = subLine(vertexIndex + 1);
            final LineString line2 = subLine(vertexIndex, vertexCount
              - vertexIndex);
            return Arrays.asList(line1, line2);
          }
        } else {
          final LineString line1 = subLine(vertexIndex + 1, point);
          final LineString line2 = subLine(point, vertexIndex, vertexCount
            - vertexIndex, null);
          return Arrays.asList(line1, line2);
        }
      } else if (geometryComponent instanceof Segment) {
        final Segment segment = (Segment)geometryComponent;
        final int segmentIndex = segment.getSegmentIndex();
        final LineString line1 = subLine(segmentIndex, point);
        final LineString line2 = subLine(point, segmentIndex + 1, vertexCount
          - segmentIndex - 1, null);
        return Arrays.asList(line1, line2);
      } else {
        return Collections.<LineString> singletonList(this);
      }
    }
  }

  @Override
  public LineString subLine(final int vertexCount) {
    return subLine(null, 0, vertexCount, null);
  }

  @Override
  public LineString subLine(final int fromVertexIndex, final int vertexCount) {
    return subLine(null, fromVertexIndex, vertexCount, null);
  }

  @Override
  public LineString subLine(final int vertexCount, final Point toPoint) {
    return subLine(null, 0, vertexCount, toPoint);
  }

  @Override
  public LineString subLine(final Point fromPoint, final int fromVertexIndex,
    int vertexCount, final Point toPoint) {
    if (fromVertexIndex + vertexCount > getVertexCount()) {
      vertexCount = getVertexCount() - fromVertexIndex;
    }
    int newVertexCount = vertexCount;
    final boolean hasFromPoint = fromPoint != null && !fromPoint.isEmpty();
    if (hasFromPoint) {
      newVertexCount++;
    }
    final boolean hasToPoint = toPoint != null && !toPoint.isEmpty();
    if (hasToPoint) {
      newVertexCount++;
    }
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (newVertexCount < 2) {
      return geometryFactory.lineString();
    } else {
      final int axisCount = getAxisCount();
      final double[] coordinates = new double[newVertexCount * axisCount];
      int vertexIndex = 0;
      if (hasFromPoint) {
        CoordinatesListUtil.setCoordinates(coordinates, axisCount,
          vertexIndex++, fromPoint);
      }
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexIndex,
        this, fromVertexIndex, vertexCount);
      vertexIndex += vertexCount;
      if (hasToPoint) {
        CoordinatesListUtil.setCoordinates(coordinates, axisCount,
          vertexIndex++, toPoint);
      }
      final LineString newLine = geometryFactory.lineString(axisCount,
        coordinates);
      GeometryProperties.copyUserData(this, newLine);
      return newLine;
    }
  }

  @Override
  public Reader<Vertex> vertices() {
    final LineStringVertex vertex = new LineStringVertex(this, -1);
    return vertex.reader();
  }

}
