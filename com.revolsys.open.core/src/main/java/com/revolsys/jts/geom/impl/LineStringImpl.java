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
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
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
  private double[] coordinates;

  public LineStringImpl(final GeometryFactory factory) {
    super(factory);
    this.coordinates = null;
  }

  public LineStringImpl(final GeometryFactory factory,
    final CoordinatesList points) {
    super(factory);
    if (points == null) {
      this.coordinates = null;
    } else {
      final int vertexCount = points.size();
      if (vertexCount == 0) {
        this.coordinates = null;
      } else if (vertexCount == 1) {
        throw new IllegalArgumentException(
          "Invalid number of points in LineString (found " + vertexCount
            + " - must be 0 or >= 2)");
      } else {
        final int axisCount = getNumAxis();
        this.coordinates = new double[axisCount * vertexCount];
        for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
          for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
            double value = points.getValue(vertexIndex, axisIndex);
            value = factory.makePrecise(axisIndex, value);
            this.coordinates[vertexIndex * axisCount + axisIndex] = value;
          }
        }
      }
    }
  }

  public LineStringImpl(final GeometryFactory factory, final int numAxis,
    final double... points) {
    super(factory);
    if (points == null) {
      this.coordinates = null;
    } else {
      final int coordinateCount = points.length;
      final int vertexCount = coordinateCount / numAxis;
      if (coordinateCount == 0) {
        this.coordinates = null;
      } else if (coordinateCount % numAxis != 0) {
        throw new IllegalArgumentException("Point array length "
          + coordinateCount + " is not a multiple of numAxis=" + numAxis);
      } else if (coordinateCount == numAxis) {
        throw new IllegalArgumentException(
          "Invalid number of points in LineString (found " + vertexCount
            + " - must be 0 or >= 2)");
      } else {
        final int axisCount = getNumAxis();
        this.coordinates = new double[axisCount * vertexCount];
        for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
          for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
            double value;
            if (axisIndex < numAxis) {
              value = points[vertexIndex * numAxis + axisIndex];
              value = factory.makePrecise(axisIndex, value);
            } else {
              value = Double.NaN;
            }
            this.coordinates[vertexIndex * axisCount + axisIndex] = value;
          }
        }
      }
    }
  }

  @Override
  public void apply(final CoordinateFilter filter) {
    final int vertexCount = getVertexCount();
    for (int i = 0; i < vertexCount; i++) {
      final Coordinates point = getCoordinate(i);
      filter.filter(point);
    }
  }

  @Override
  public void apply(final CoordinateSequenceFilter filter) {
    if (!isEmpty()) {
      final int vertexCount = getVertexCount();
      final CoordinatesList points = getCoordinatesList();
      for (int i = 0; i < vertexCount; i++) {
        filter.filter(points, i);
        if (filter.isDone()) {
          break;
        }
      }
      if (filter.isGeometryChanged()) {
        geometryChanged();
      }
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
    if (coordinates != null) {
      line.coordinates = coordinates.clone();
    }
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
    return comp.compare(getPointList(), line.getPointList());
  }

  @Override
  protected Envelope computeEnvelopeInternal() {
    if (isEmpty()) {
      return new Envelope();
    } else {
      return getPointList().expandEnvelope(new Envelope());
    }
  }

  @Override
  public LineString convert(final GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (sourceGeometryFactory == geometryFactory) {
      return this;
    } else {
      return copy(geometryFactory);
    }
  }

  protected double[] convertCoordinates(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return this.coordinates;
    } else {
      geometryFactory = getNonZeroGeometryFactory(geometryFactory);
      double[] targetCoordinates;
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory.getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        return this.coordinates;
      } else {
        final int sourceNumAxis = getNumAxis();
        final int targetNumAxis = geometryFactory.getNumAxis();
        targetCoordinates = new double[targetNumAxis * getVertexCount()];
        coordinatesOperation.perform(sourceNumAxis, this.coordinates,
          targetNumAxis, targetCoordinates);
        return targetCoordinates;
      }
    }
  }

  @Override
  public LineString copy(final GeometryFactory geometryFactory) {
    if (isEmpty()) {
      return geometryFactory.lineString();
    } else {
      final double[] coordinates = convertCoordinates(geometryFactory);
      final int numAxis = getNumAxis();
      return geometryFactory.lineString(numAxis, coordinates);
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
  public Coordinates getCoordinate(final int vertexIndex) {
    if (isEmpty()) {
      return null;
    } else {
      final int numAxis = getNumAxis();
      final double[] coordinates = new double[numAxis];
      System.arraycopy(this.coordinates, vertexIndex * numAxis, coordinates, 0,
        numAxis);
      return new DoubleCoordinates(coordinates);
    }
  }

  @Override
  public double getCoordinate(int vertexIndex, final int axisIndex) {
    if (isEmpty()) {
      return Double.NaN;
    } else {
      final int numAxis = getNumAxis();
      if (axisIndex < 0 || axisIndex >= numAxis) {
        return Double.NaN;
      } else {
        final int numPoints = getVertexCount();
        if (vertexIndex < numPoints) {
          while (vertexIndex < 0) {
            vertexIndex += numPoints;
          }
          return coordinates[vertexIndex * numAxis + axisIndex];
        } else {
          return Double.NaN;
        }
      }
    }
  }

  @Override
  public Coordinates[] getCoordinateArray() {
    return getPointList().toCoordinateArray();
  }

  @Override
  public CoordinatesList getCoordinatesList() {
    return getPointList();
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
    return CGAlgorithms.length(getPointList());
  }

  @Override
  public Point getPoint(final int vertexIndex) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final Coordinates coordinate = getCoordinate(vertexIndex);
    return geometryFactory.point(coordinate);
  }

  @Override
  public CoordinatesList getPointList() {
    if (coordinates == null) {
      return new DoubleCoordinatesList(getNumAxis());
    } else {
      return new DoubleCoordinatesList(getNumAxis(), coordinates);
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
      final int vertexIndex = vertexId[0];
      return getVertex(vertexIndex);
    }
    return null;
  }

  @Override
  public AbstractVertex getVertex(int vertexIndex) {
    final int vertexCount = getVertexCount();
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
    if (isEmpty()) {
      return 0;
    } else {
      return coordinates.length / getNumAxis();
    }
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
    return coordinates == null;
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
    final int vertexCount = getVertexCount();
    final CoordinatesList points = getPointList();
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
    final CoordinatesList points = getPointList();
    final CoordinatesList reversePoints = points.reverse();
    final GeometryFactory geometryFactory = getGeometryFactory();
    final LineString reverseLine = geometryFactory.lineString(reversePoints);
    return reverseLine;
  }

  @Override
  public Iterable<Vertex> vertices() {
    return new LineStringVertexIterable(this);
  }

}
