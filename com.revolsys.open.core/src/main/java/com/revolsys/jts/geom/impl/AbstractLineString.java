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
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.gis.model.data.equals.NumberEquals;
import com.revolsys.io.Reader;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.CoordinateSequenceComparator;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.Dimension;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.segment.LineStringSegment;
import com.revolsys.jts.geom.segment.Segment;
import com.revolsys.jts.geom.vertex.AbstractVertex;
import com.revolsys.jts.geom.vertex.LineStringVertex;
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
  public int compareToSameClass(final Geometry o) {
    final LineString line = (LineString)o;
    // MD - optimized implementation
    int i = 0;
    int j = 0;
    final int vertexCount = getVertexCount();
    while (i < vertexCount && j < line.getVertexCount()) {
      final int comparison = getCoordinate(i).compareTo(line.getCoordinate(j));
      if (comparison != 0) {
        return comparison;
      }
      i++;
      j++;
    }
    if (i < vertexCount) {
      return 1;
    }
    if (j < line.getVertexCount()) {
      return -1;
    }
    return 0;
  }

  @Override
  public int compareToSameClass(final Geometry o,
    final CoordinateSequenceComparator comp) {
    final LineString line = (LineString)o;
    return comp.compare(getCoordinatesList(), line.getCoordinatesList());
  }

  @Override
  protected BoundingBox computeBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return new Envelope(geometryFactory);
    } else {
      return new Envelope(geometryFactory, vertices());
    }
  }

  @Override
  public LineString convert(final GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (geometryFactory == null || sourceGeometryFactory == geometryFactory) {
      return this;
    } else {
      return copy(geometryFactory);
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
        final int targetAxisCount = geometryFactory.getAxisCount();
        targetCoordinates = new double[targetAxisCount * getVertexCount()];
        coordinatesOperation.perform(sourceAxisCount, coordinates,
          targetAxisCount, targetCoordinates);
        return targetCoordinates;
      }
    }
  }

  @Override
  public LineString copy(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return this.clone();
    } else if (isEmpty()) {
      return geometryFactory.lineString();
    } else {
      final double[] coordinates = convertCoordinates(geometryFactory);
      final int axisCount = getAxisCount();
      return geometryFactory.lineString(axisCount, coordinates);
    }
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
      final Coordinates point = getCoordinate(i);
      final Coordinates otherPoint = otherLineString.getCoordinate(i);
      if (!equal(point, otherPoint, tolerance)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean equalsExact3d(final Geometry geometry) {
    if (geometry == this) {
      return true;
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      final int vertexCount = getVertexCount();
      if (vertexCount == line.getVertexCount()) {
        for (int i = 0; i < line.getVertexCount(); i++) {
          for (int axisIndex = 0; axisIndex < 3; axisIndex++) {
            final double value1 = getCoordinate(i, axisIndex);
            final double value2 = line.getCoordinate(i, axisIndex);
            if (!NumberEquals.equal(value1, value2)) {
              return false;
            }
          }
        }
      }
    }
    return false;
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
    } else {
      return getCoordinate(0);
    }
  }

  @Override
  public Coordinates[] getCoordinateArray() {
    return getCoordinatesList().toCoordinateArray();
  }

  public abstract double[] getCoordinates();

  @Override
  public CoordinatesList getCoordinatesList() {
    if (isEmpty()) {
      return new DoubleCoordinatesList(getAxisCount());
    } else {
      return new DoubleCoordinatesList(getAxisCount(), getCoordinates());
    }
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
   *  Returns the length of this <code>LineString</code>
   *
   *@return the length of the linestring
   */
  @Override
  public double getLength() {
    return CGAlgorithms.length(getCoordinatesList());
  }

  @Override
  public double getM(final int vertexIndex) {
    return getCoordinate(vertexIndex, 3);
  }

  @Override
  public Point getPoint(final int vertexIndex) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Coordinates coordinate = getCoordinate(vertexIndex);
    return geometryFactory.point(coordinate);
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
    final CoordinatesList points = getCoordinatesList();
    final boolean counterClockwise = points.isCounterClockwise();
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
   * Normalizes a LineString.  A normalized linestring
   * has the first point which is not equal to it's reflected point
   * less than the reflected point.
   */
  @Override
  public LineString normalize() {
    final int vertexCount = getVertexCount();
    final CoordinatesList points = getCoordinatesList();
    for (int i = 0; i < vertexCount / 2; i++) {
      final int j = vertexCount - 1 - i;
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
    return getCoordinatesList();
  }

  /**
   * Creates a {@link LineString} whose coordinates are in the reverse
   * order of this objects
   *
   * @return a {@link LineString} with coordinates in the reverse order
   */
  @Override
  public LineString reverse() {
    final CoordinatesList points = getCoordinatesList();
    final CoordinatesList reversePoints = points.reverse();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LineString reverseLine = geometryFactory.lineString(reversePoints);
    return reverseLine;
  }

  @Override
  public Reader<Segment> segments() {
    final LineStringSegment iterator = new LineStringSegment(this, -1);
    return new IteratorReader<Segment>(iterator);
  }

  @Override
  public Reader<Vertex> vertices() {
    final LineStringVertex vertex = new LineStringVertex(this, -1);
    return vertex.reader();
  }

}