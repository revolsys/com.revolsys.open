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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import com.revolsys.collection.CollectionUtil;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RayCrossingCounter;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.graph.linemerge.LineMerger;
import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.editor.LineStringEditor;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.metrics.PointLineStringMetrics;
import com.revolsys.geometry.model.prep.PreparedLineString;
import com.revolsys.geometry.model.segment.LineSegmentDouble;
import com.revolsys.geometry.model.segment.LineStringSegment;
import com.revolsys.geometry.model.segment.Segment;
import com.revolsys.geometry.model.vertex.AbstractVertex;
import com.revolsys.geometry.model.vertex.LineStringVertex;
import com.revolsys.geometry.model.vertex.Vertex;
import com.revolsys.geometry.operation.BoundaryOp;
import com.revolsys.geometry.util.GeometryProperties;
import com.revolsys.geometry.util.LineStringUtil;
import com.revolsys.util.MathUtil;
import com.revolsys.util.Property;
import com.revolsys.util.number.Doubles;

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
public interface LineString extends Lineal {
  @SuppressWarnings("unchecked")
  static <G extends Geometry> G newLineString(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof LineString) {
      return (G)value;
    } else if (value instanceof Geometry) {
      throw new IllegalArgumentException(
        ((Geometry)value).getGeometryType() + " cannot be converted to a LineString");
    } else {
      final String string = DataTypes.toString(value);
      return (G)GeometryFactory.DEFAULT.geometry(string, false);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V appendVertex(Point newPoint, final int... geometryId) {
    if (geometryId.length == 0) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (newPoint == null || newPoint.isEmpty()) {
        return (V)this;
      } else if (isEmpty()) {
        return newPoint.convertGeometry(geometryFactory);
      } else {
        newPoint = newPoint.convertGeometry(geometryFactory);
        final int vertexCount = getVertexCount();
        final double[] coordinates = getCoordinates();
        final int axisCount = getAxisCount();
        final double[] newCoordinates = new double[axisCount * (vertexCount + 1)];

        final int length = vertexCount * axisCount;
        System.arraycopy(coordinates, 0, newCoordinates, 0, length);
        CoordinatesListUtil.setCoordinates(newCoordinates, axisCount, vertexCount, newPoint);

        return (V)newLineString(newCoordinates);
      }
    } else {
      throw new IllegalArgumentException("Geometry id's for " + getGeometryType()
        + " must have length 0. " + Arrays.toString(geometryId));
    }
  }

  @Override
  default Lineal applyLineal(final Function<LineString, LineString> function) {
    final LineString newLine = function.apply(this);
    return newLine;
  }

  @Override
  LineString clone();

  @Override
  default int compareToSameClass(final Geometry geometry) {
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

  default double[] convertCoordinates(GeometryFactory geometryFactory) {
    final GeometryFactory sourceGeometryFactory = getGeometryFactory();
    final double[] coordinates = getCoordinates();
    if (isEmpty()) {
      return coordinates;
    } else {
      geometryFactory = Geometry.getNonZeroGeometryFactory(this, geometryFactory);
      double[] targetCoordinates;
      final CoordinatesOperation coordinatesOperation = sourceGeometryFactory
        .getCoordinatesOperation(geometryFactory);
      if (coordinatesOperation == null) {
        return coordinates;
      } else {
        final int sourceAxisCount = getAxisCount();
        targetCoordinates = new double[sourceAxisCount * getVertexCount()];
        coordinatesOperation.perform(sourceAxisCount, coordinates, sourceAxisCount,
          targetCoordinates);
        return targetCoordinates;
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V copy(final GeometryFactory geometryFactory) {
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

  default int copyCoordinates(final int axisCount, final double nanValue,
    final double[] destCoordinates, int destOffset) {
    if (isEmpty()) {
      return destOffset;
    } else {
      for (int vertexIndex = 0; vertexIndex < getVertexCount(); vertexIndex++) {
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          double coordinate = getCoordinate(vertexIndex, axisIndex);
          if (Double.isNaN(coordinate)) {
            coordinate = nanValue;
          }
          destCoordinates[destOffset++] = coordinate;
        }
      }
      return destOffset;
    }
  }

  default int copyCoordinatesReverse(final int axisCount, final double nanValue,
    final double[] destCoordinates, int destOffset) {
    if (isEmpty()) {
      return destOffset;
    } else {
      for (int vertexIndex = getVertexCount() - 1; vertexIndex >= 0; vertexIndex--) {
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          double coordinate = getCoordinate(vertexIndex, axisIndex);
          if (Double.isNaN(coordinate)) {
            coordinate = nanValue;
          }
          destCoordinates[destOffset++] = coordinate;
        }
      }
      return destOffset;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V deleteVertex(final int... vertexId) {
    if (vertexId.length == 1) {
      final int vertexIndex = vertexId[0];
      return (V)deleteVertex(vertexIndex);
    } else {
      throw new IllegalArgumentException("Vertex id's for " + getGeometryType()
        + " must have length 1. " + Arrays.toString(vertexId));
    }
  }

  default LineString deleteVertex(final int vertexIndex) {
    if (isEmpty()) {
      throw new IllegalArgumentException("Cannot delete vertex for empty LineString");
    } else {
      final int vertexCount = getVertexCount();
      if (vertexCount <= 2) {
        throw new IllegalArgumentException("LineString must have a minimum of 2 vertices");
      } else if (vertexIndex >= 0 && vertexIndex < vertexCount) {
        final double[] coordinates = getCoordinates();
        final int axisCount = getAxisCount();
        final double[] newCoordinates = new double[axisCount * (vertexCount - 1)];
        final int beforeLength = vertexIndex * axisCount;
        if (vertexIndex > 0) {
          System.arraycopy(coordinates, 0, newCoordinates, 0, beforeLength);
        }
        final int sourceIndex = (vertexIndex + 1) * axisCount;
        final int length = (vertexCount - vertexIndex - 1) * axisCount;
        System.arraycopy(coordinates, sourceIndex, newCoordinates, beforeLength, length);

        return newLineString(newCoordinates);
      } else {
        throw new IllegalArgumentException("Vertex index must be between 0 and " + vertexCount);
      }
    }
  }

  @Override
  default double distance(final Geometry geometry, final double terminateDistance) {
    if (geometry instanceof Point) {
      final Point point = (Point)geometry;
      return distance(point, terminateDistance);
    } else if (geometry instanceof LineString) {
      final LineString line = (LineString)geometry;
      return distance(line, terminateDistance);
    } else {
      return geometry.distance(this, terminateDistance);
    }
  }

  default double distance(final int index, final Point point) {
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

  default double distance(LineString line, final double terminateDistance) {
    if (isEmpty()) {
      return 0.0;
    } else if (Property.isEmpty(line)) {
      return 0.0;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      line = line.convertGeometry(geometryFactory, 2);
      double minDistance = Double.MAX_VALUE;
      for (final Segment segment1 : segments()) {
        for (final Segment segment2 : line.segments()) {
          final double distance = segment1.distance(segment2);
          if (distance < minDistance) {
            minDistance = distance;
            if (minDistance <= terminateDistance) {
              return minDistance;
            }
          }
        }
      }
      return minDistance;
    }
  }

  default double distance(final Point point) {
    return distance(point, 0.0);
  }

  default double distance(Point point, final double terminateDistance) {
    if (isEmpty()) {
      return 0.0;
    } else if (Property.isEmpty(point)) {
      return 0.0;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertGeometry(geometryFactory, 2);
      double minDistance = Double.MAX_VALUE;
      for (final Segment segment : segments()) {
        final double distance = segment.distance(point);
        if (distance < minDistance) {
          minDistance = distance;
          if (minDistance <= terminateDistance) {
            return minDistance;
          }
        }
      }
      return minDistance;
    }
  }

  default double distanceAlong(final Point point) {
    if (isEmpty() && point.isEmpty()) {
      return Double.MAX_VALUE;
    } else {
      double distanceAlongSegments = 0;
      double closestDistance = Double.MAX_VALUE;
      double distanceAlong = 0;
      final double resolutionXy = getGeometryFactory().getResolutionXy();
      for (final Segment segment : segments()) {
        if (segment.equalsVertex(0, point)) {
          return distanceAlongSegments;
        } else {
          final double segmentLength = segment.getLength();
          final double distance = segment.distance(point);
          final double projectionFactor = segment.projectionFactor(point);
          if (distance < resolutionXy) {
            return distanceAlongSegments + segment.getPoint(0).distance(point);
          } else if (distance < closestDistance) {
            closestDistance = distance;
            if (projectionFactor == 0) {
              distanceAlong = distanceAlongSegments;
            } else if (projectionFactor < 0) {
              if (segment.getSegmentIndex() == 0) {
                distanceAlong = segmentLength * projectionFactor;
              } else {
                distanceAlong = distanceAlongSegments;
              }
            } else if (projectionFactor >= 1) {
              if (segment.isLineEnd()) {
                distanceAlong = distanceAlongSegments + segmentLength * projectionFactor;
              } else {
                distanceAlong = distanceAlongSegments + segmentLength;
              }
            } else {
              distanceAlong = distanceAlongSegments + segmentLength * projectionFactor;
            }
          }
          distanceAlongSegments += segmentLength;
        }
      }
      return distanceAlong;
    }
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
        final LineString line = (LineString)geometry;
        final int vertexCount = getVertexCount();
        final int vertexCount2 = line.getVertexCount();
        if (vertexCount == vertexCount2) {
          for (int i = 0; i < vertexCount2; i++) {
            for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
              final double value1 = getCoordinate(i, axisIndex);
              final double value2 = line.getCoordinate(i, axisIndex);
              if (!Doubles.equal(value1, value2)) {
                return false;
              }
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  default boolean equals(final int axisCount, final int vertexIndex, final Point point) {
    for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
      final double value = getCoordinate(vertexIndex, axisIndex);
      final double value2 = point.getCoordinate(axisIndex);
      if (!Doubles.equal(value, value2)) {
        return false;
      }
    }
    return true;
  }

  @Override
  default boolean equalsExact(final Geometry other, final double tolerance) {
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

  default boolean equalsVertex(final int vertexIndex, final double... coordinates) {
    if (isEmpty() || coordinates == null || coordinates.length < 2) {
      return false;
    } else {
      for (int axisIndex = 0; axisIndex < coordinates.length; axisIndex++) {
        final double coordinate = coordinates[axisIndex];
        final double matchCoordinate = getCoordinate(vertexIndex, axisIndex);
        if (!Doubles.equal(coordinate, matchCoordinate)) {
          return false;
        }
      }
      return true;
    }
  }

  default boolean equalsVertex(final int vertexIndex, final double x, final double y) {
    if (Doubles.equal(getX(vertexIndex), x)) {
      if (Doubles.equal(getY(vertexIndex), y)) {
        return true;
      }
    }
    return false;
  }

  default boolean equalsVertex(final int axisCount, final int vertexIndex1,
    final int vertexIndex2) {
    if (isEmpty()) {
      return false;
    } else {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double coordinate1 = getCoordinate(vertexIndex1, axisIndex);
        final double coordinate2 = getCoordinate(vertexIndex2, axisIndex);
        if (!Doubles.equal(coordinate1, coordinate2)) {
          return false;
        }
      }
      return true;
    }
  }

  default boolean equalsVertex(final int axisCount, final int vertexIndex, final LineString line2,
    final int vertexIndex2) {
    if (Property.isEmpty(line2)) {
      return false;
    } else {
      final Vertex vertex2 = line2.getVertex(vertexIndex2);
      return equalsVertex(axisCount, vertexIndex, vertex2);
    }
  }

  default boolean equalsVertex(final int axisCount, final int vertexIndex, Point point) {
    if (isEmpty() || Property.isEmpty(point)) {
      return false;
    } else {
      final GeometryFactory geometryFactory = getGeometryFactory();
      point = point.convertGeometry(geometryFactory, axisCount);
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final double coordinate = point.getCoordinate(axisIndex);
        final double matchCoordinate = getCoordinate(vertexIndex, axisIndex);
        if (!Doubles.equal(coordinate, matchCoordinate)) {
          return false;
        }
      }
      return true;
    }
  }

  default boolean equalsVertex(final int vertexIndex, final Point point) {
    if (Property.isEmpty(point)) {
      return false;
    } else {
      final int axisCount = point.getAxisCount();
      return equalsVertex(axisCount, vertexIndex, point);
    }
  }

  /**
   * Gets the boundary of this geometry. The boundary of a lineal geometry is
   * always a zero-dimensional geometry (which may be empty).
   *
   * @return the boundary geometry
   * @see Geometry#getBoundary
   */
  @Override
  default Geometry getBoundary() {
    return new BoundaryOp(this).getBoundary();
  }

  @Override
  default int getBoundaryDimension() {
    if (isClosed()) {
      return Dimension.FALSE;
    }
    return 0;
  }

  default ClockDirection getClockDirection() {
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
      return ClockDirection.CLOCKWISE;
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
    if (counterClockwise) {
      return ClockDirection.COUNTER_CLOCKWISE;
    } else {
      return ClockDirection.CLOCKWISE;
    }
  }

  double getCoordinate(int vertexIndex, final int axisIndex);

  @Override
  default double getCoordinate(final int partIndex, final int vertexIndex, final int axisIndex) {
    if (partIndex == 0) {
      return getCoordinate(vertexIndex, axisIndex);
    } else {
      return Double.NaN;
    }
  }

  default double getCoordinateFast(final int vertexIndex, final int axisIndex) {
    return getCoordinate(vertexIndex, axisIndex);
  }

  double[] getCoordinates();

  default double[] getCoordinates(final int axisCount) {
    if (axisCount == getAxisCount()) {
      return getCoordinates();
    } else if (isEmpty()) {
      return new double[0];
    } else {
      final int vertexCount = getVertexCount();
      final double[] coordinates = new double[axisCount * vertexCount];
      int i = 0;
      for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
        for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
          final double coordinate = getCoordinate(vertexIndex, axisIndex);
          coordinates[i++] = coordinate;
        }
      }
      return coordinates;
    }
  }

  @Override
  default DataType getDataType() {
    return DataTypes.LINE_STRING;
  }

  @Override
  default int getDimension() {
    return 1;
  }

  default Point getFromPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return getPoint(0);
    }
  }

  /**
   * Returns the length of this <code>LineString</code>
   *
   * @return the length of the linestring
   */
  @Override
  default double getLength() {
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
  default double getLength(final Unit<Length> unit) {
    double length = 0;
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem instanceof GeographicCoordinateSystem) {
      final int vertexCount = getVertexCount();
      if (vertexCount > 1) {
        double lon0 = getX(0);
        double lat0 = getY(0);
        for (int i = 1; i < vertexCount; i++) {
          final double lon1 = getX(i);
          final double lat1 = getY(i);
          length += GeographicCoordinateSystem.distanceMetres(lon0, lat0, lon1, lat1);
          lon0 = lon1;
          lat0 = lat1;
        }
      }
      final Measure<Length> lengthMeasure = Measure.valueOf(length, SI.METRE);
      length = lengthMeasure.doubleValue(unit);
    } else if (coordinateSystem instanceof ProjectedCoordinateSystem) {
      final ProjectedCoordinateSystem projectedCoordinateSystem = (ProjectedCoordinateSystem)coordinateSystem;
      final Unit<Length> lengthUnit = projectedCoordinateSystem.getLengthUnit();

      length = getLength();
      final Measure<Length> lengthMeasure = Measure.valueOf(length, lengthUnit);
      length = lengthMeasure.doubleValue(unit);
    } else {
      length = getLength();
    }
    return length;
  }

  @Override
  default LineString getLineString(final int partIndex) {
    if (partIndex == 0) {
      return this;
    } else {
      return null;
    }
  }

  default double getM(final int vertexIndex) {
    return getCoordinate(vertexIndex, 3);
  }

  default PointLineStringMetrics getMetrics(Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    point = point.convertGeometry(geometryFactory, 2);
    if (isEmpty() && point.isEmpty()) {
      return PointLineStringMetrics.EMPTY;
    } else {
      double lineLength = 0;
      double closestDistance = Double.MAX_VALUE;
      double distanceAlong = 0;
      Side side = null;
      final double resolutionXy;
      if (geometryFactory.isGeographics()) {
        resolutionXy = 0.0000001;
      } else {
        resolutionXy = 0.001;
      }
      for (final Segment segment : segments()) {
        final double distance = segment.distance(point);
        final double segmentLength = segment.getLength();
        final double projectionFactor = segment.projectionFactor(point);
        final boolean isEnd = segment.isLineEnd();
        if (segment.isLineStart()) {
          if (isEnd || projectionFactor <= 1) {
            if (distance < resolutionXy) {
              side = null;
            } else {
              side = segment.getSide(point);
            }
            closestDistance = distance;
            if (projectionFactor <= 1 || isEnd) {
              distanceAlong = segmentLength * projectionFactor;
            } else {
              distanceAlong = segmentLength;
            }
          }
        } else if (distance < closestDistance) {
          if (isEnd || projectionFactor <= 1) {
            closestDistance = distance;
            if (distance == 0 || distance < resolutionXy) {
              side = null;
            } else {
              side = segment.getSide(point);
            }
            // TODO handle intermediate cases right right hand bends in lines
            if (projectionFactor == 0) {
              distanceAlong = lineLength;
            } else if (projectionFactor < 0) {
              distanceAlong = lineLength;
            } else if (projectionFactor >= 1) {
              if (isEnd) {
                distanceAlong = lineLength + segmentLength * projectionFactor;
              } else {
                distanceAlong = lineLength + segmentLength;
              }
            } else {
              distanceAlong = lineLength + segmentLength * projectionFactor;
            }
          }
        }
        lineLength += segmentLength;
      }
      return new PointLineStringMetrics(lineLength, distanceAlong, closestDistance, side);
    }
  }

  default int getMinVertexCount() {
    return 2;
  }

  @Override
  default Point getPoint() {
    if (isEmpty()) {
      return null;
    } else {
      return getPoint(0);
    }
  }

  default Point getPoint(final End lineEnd) {
    if (End.isFrom(lineEnd)) {
      return getFromPoint();
    } else {
      return getToPoint();
    }
  }

  default Point getPoint(int vertexIndex) {
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
  default Point getPointWithin() {
    final GeometryFactory geometryFactory = this.getGeometryFactory();
    if (this.isEmpty()) {
      return geometryFactory.point();
    } else {
      final int numPoints = getVertexCount();
      if (numPoints > 1) {
        final double totalLength = getLength();
        final double midPointLength = totalLength / 2;
        double currentLength = 0;
        for (int i = 1; i < numPoints && currentLength < midPointLength; i++) {
          final Point p1 = getPoint(i - 1);
          final Point p2 = getPoint(i);
          final double segmentLength = p1.distance(p2);
          if (segmentLength + currentLength >= midPointLength) {
            final Point midPoint = LineSegmentUtil.project(geometryFactory, p1, p2,
              (midPointLength - currentLength) / segmentLength);
            return geometryFactory.point(midPoint);

          }
          currentLength += segmentLength;
        }
        return geometryFactory.point();
      } else {
        return this.getPoint(0);
      }
    }
  }

  @Override
  default LineStringSegment getSegment(final int... segmentId) {
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
  default int getSegmentCount() {
    if (isEmpty()) {
      return 0;
    } else {
      return getVertexCount() - 1;
    }
  }

  default Side getSide(Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    point = point.convertGeometry(geometryFactory, 2);
    Side side = null;
    if (!isEmpty() && !point.isEmpty()) {
      double closestDistance = Double.MAX_VALUE;
      final double resolutionXy;
      if (geometryFactory.isGeographics()) {
        resolutionXy = 0.0000001;
      } else {
        resolutionXy = 0.001;
      }
      for (final Segment segment : segments()) {
        final double distance = segment.distance(point);
        final double projectionFactor = segment.projectionFactor(point);
        final boolean isEnd = segment.isLineEnd();
        if (segment.isLineStart()) {
          if (isEnd || projectionFactor <= 1) {
            if (distance < resolutionXy) {
              side = null;
            } else {
              side = segment.getSide(point);
            }
            closestDistance = distance;
          }
        } else if (distance < closestDistance) {
          if (isEnd || projectionFactor <= 1) {
            closestDistance = distance;
            if (distance == 0 || distance < resolutionXy) {
              side = null;
            } else {
              side = segment.getSide(point);
            }
          }
        }
      }
    }
    return side;
  }

  default Point getToPoint() {
    if (isEmpty()) {
      return null;
    } else {
      final int vertexCount = getVertexCount();
      return getPoint(vertexCount - 1);
    }
  }

  @Override
  default AbstractVertex getToVertex(final int... vertexId) {
    if (vertexId.length == 1) {
      int vertexIndex = vertexId[0];
      final int vertexCount = getVertexCount();
      vertexIndex = vertexCount - vertexIndex - 1;
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
  default AbstractVertex getVertex(final int... vertexId) {
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

  default double getX(final int vertexIndex) {
    return getCoordinate(vertexIndex, X);
  }

  default double getY(final int vertexIndex) {
    return getCoordinate(vertexIndex, Y);
  }

  default double getZ(final int vertexIndex) {
    return getCoordinate(vertexIndex, Z);
  }

  default boolean hasVertex(final Point point) {
    final int vertexCount = getVertexCount();
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      if (equalsVertex(2, vertexIndex, point)) {
        return true;
      }
    }
    return false;
  }

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V insertVertex(Point newPoint, final int... vertexId) {
    if (vertexId.length == 1) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (newPoint == null || newPoint.isEmpty()) {
        return (V)this;
      } else if (isEmpty()) {
        return newPoint.convertGeometry(geometryFactory);
      } else {
        newPoint = newPoint.convertGeometry(geometryFactory);
        final int vertexCount = getVertexCount();
        final double[] coordinates = getCoordinates();
        final int axisCount = getAxisCount();
        final double[] newCoordinates = new double[axisCount * (vertexCount + 1)];

        final int vertexIndex = vertexId[0];

        final int beforeLength = vertexIndex * axisCount;
        System.arraycopy(coordinates, 0, newCoordinates, 0, beforeLength);

        CoordinatesListUtil.setCoordinates(newCoordinates, axisCount, vertexIndex, newPoint);

        final int afterSourceIndex = vertexIndex * axisCount;
        final int afterNewIndex = (vertexIndex + 1) * axisCount;
        final int afterLength = (vertexCount - vertexIndex) * axisCount;
        System.arraycopy(coordinates, afterSourceIndex, newCoordinates, afterNewIndex, afterLength);

        return (V)newLineString(newCoordinates);
      }
    } else {
      throw new IllegalArgumentException("Geometry id's for " + getGeometryType()
        + " must have length 1. " + Arrays.toString(vertexId));
    }
  }

  @Override
  default boolean intersects(final BoundingBox boundingBox) {
    if (isEmpty() || boundingBox.isEmpty()) {
      return false;
    } else {
      final GeometryFactory geometryFactory = boundingBox.getGeometryFactory().convertAxisCount(2);
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

  default boolean isClockwise() {
    final ClockDirection clockDirection = getClockDirection();
    return clockDirection.isClockwise();
  }

  @Override
  default boolean isClosed() {
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

  default boolean isCounterClockwise() {
    final ClockDirection clockDirection = getClockDirection();
    return clockDirection.isCounterClockwise();
  }

  @Override
  default boolean isEmpty() {
    return getVertexCount() == 0;
  }

  @Override
  default boolean isEquivalentClass(final Geometry other) {
    return other instanceof LineString;
  }

  default boolean isLeft(final Point point) {
    for (final Segment segment : segments()) {
      if (!new LineSegmentDouble(segment.getPoint(0), point).crosses(this)
        && !new LineSegmentDouble(segment.getPoint(1), point).crosses(this)) {
        final int orientation = segment.orientationIndex(point);
        if (orientation == 1) {
          return true;
        } else {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Tests whether a point lies on the line segments defined by a list of
   * coordinates.
   *
   * @return true if the point is a vertex of the line or lies in the interior
   *         of a line segment in the linestring
   */
  default boolean isOnLine(final Point p) {
    final LineIntersector lineIntersector = new RobustLineIntersector();
    for (final Segment segment : segments()) {
      final Point p0 = segment.getPoint(0);
      final Point p1 = segment.getPoint(1);
      lineIntersector.computeIntersection(p, p0, p1);
      if (lineIntersector.hasIntersection()) {
        return true;
      }
    }
    return false;
  }

  default boolean isPointInRing(final Point point) {
    return RayCrossingCounter.locatePointInRing(point, this) != Location.EXTERIOR;
  }

  default boolean isRing() {
    return isClosed() && isSimple();
  }

  @Override
  default Iterable<LineString> lineStrings() {
    return Collections.singletonList(this);
  }

  @Override
  default Location locate(final Point point) {
    // bounding-box check
    if (point.intersects(getBoundingBox())) {
      if (!isClosed()) {
        if (point.equals(getVertex(0)) || point.equals(getVertex(-1))) {
          return Location.BOUNDARY;
        }
      }
      if (isOnLine(point)) {
        return Location.INTERIOR;
      }
    }
    return Location.EXTERIOR;
  }

  /**
   * Merge two lines that share common coordinates at either the start or end.
   * If the lines touch only at their start coordinates, the line2 will be
   * reversed and joined before the start of line1. If the two lines touch only
   * at their end coordinates, the line2 will be reversed and joined after the
   * end of line1.
   *
   * @param line1 The first line.
   * @param line2 The second line.
   * @return The new line string
   */
  default LineString merge(final LineString line2) {
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
      newVertexCount = CoordinatesListUtil.append(axisCount, line2, 0, coordinates, 0,
        vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1, coordinates, newVertexCount,
        vertexCount1 - 1);
    } else if (line2From.equals(2, line1To)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0, coordinates, 0, vertexCount1);
      newVertexCount = CoordinatesListUtil.append(axisCount, line2, 1, coordinates, newVertexCount,
        vertexCount2 - 1);
    } else if (line1From.equals(2, line2From)) {
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line2, 0, coordinates, 0,
        vertexCount2);
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 1, coordinates, newVertexCount,
        vertexCount1 - 1);
    } else if (line1To.equals(2, line2To)) {
      newVertexCount = CoordinatesListUtil.append(axisCount, this, 0, coordinates, newVertexCount,
        vertexCount1);
      newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line2, 1, coordinates,
        newVertexCount, vertexCount2 - 1);
    } else {
      throw new IllegalArgumentException("lines don't touch\n" + this + "\n" + line2);

    }
    final LineString newLine = newLineString(newVertexCount, coordinates);
    GeometryProperties.copyUserData(this, newLine);
    return newLine;
  }

  default LineString merge(final Point point, final LineString line2) {
    if (isEmpty() || Property.isEmpty(line2) || Property.isEmpty(point)) {
      return newLineStringEmpty();
    } else {
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
      if (line1To.equals(2, line2From) && line1To.equals(2, point)) {
        // -->*--> = ---->
        newVertexCount = CoordinatesListUtil.append(axisCount, this, 0, coordinates, 0,
          vertexCount1);
        newVertexCount = CoordinatesListUtil.append(axisCount, line2, 1, coordinates,
          newVertexCount, vertexCount2 - 1);
      } else if (line1From.equals(2, line2To) && line1From.equals(2, point)) {
        // <--*<-- = <----
        newVertexCount = CoordinatesListUtil.append(axisCount, line2, 0, coordinates, 0,
          vertexCount2);
        newVertexCount = CoordinatesListUtil.append(axisCount, this, 1, coordinates, newVertexCount,
          vertexCount1 - 1);
      } else if (line1From.equals(2, line2From) && line1From.equals(2, point)) {
        // <--*--> = <----
        newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line2, 0, coordinates, 0,
          vertexCount2);
        newVertexCount = CoordinatesListUtil.append(axisCount, this, 1, coordinates, newVertexCount,
          vertexCount1 - 1);
      } else if (line1To.equals(2, line2To) && line1To.equals(2, point)) {
        // -->*<-- = ---->
        newVertexCount = CoordinatesListUtil.append(axisCount, this, 0, coordinates, newVertexCount,
          vertexCount1);
        newVertexCount = CoordinatesListUtil.appendReverse(axisCount, line2, 1, coordinates,
          newVertexCount, vertexCount2 - 1);
      } else {
        throw new IllegalArgumentException("lines don't touch\n" + this + "\n" + line2);
      }
      final LineString newLine = newLineString(newVertexCount, coordinates);
      GeometryProperties.copyUserData(this, newLine);
      return newLine;
    }
  }

  @Override
  default LineString move(final double... deltas) {
    if (deltas == null || isEmpty()) {
      return this;
    } else {
      final double[] coordinates = moveCoordinates(deltas);
      return newLineString(coordinates);
    }
  }

  default double[] moveCoordinates(final double... deltas) {
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

  @Override
  @SuppressWarnings("unchecked")
  default <V extends Geometry> V moveVertex(final Point newPoint, final int... vertexId) {
    if (vertexId.length == 1) {
      final int vertexIndex = vertexId[0];
      return (V)moveVertex(newPoint, vertexIndex);
    } else {
      throw new IllegalArgumentException("Vertex id's for " + getGeometryType()
        + " must have length 1. " + Arrays.toString(vertexId));
    }
  }

  default LineString moveVertex(Point newPoint, final int vertexIndex) {
    if (newPoint == null || newPoint.isEmpty()) {
      return this;
    } else if (isEmpty()) {
      throw new IllegalArgumentException("Cannot move vertex for empty LineString");
    } else {
      final int vertexCount = getVertexCount();
      if (vertexIndex >= 0 && vertexIndex < vertexCount) {
        final GeometryFactory geometryFactory = getGeometryFactory();
        newPoint = newPoint.convertGeometry(geometryFactory);

        final double[] coordinates = getCoordinates();
        final int axisCount = getAxisCount();
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexIndex, newPoint);
        return newLineString(coordinates);
      } else {
        throw new IllegalArgumentException("Vertex index must be between 0 and " + vertexCount);
      }
    }
  }

  @Override
  default BoundingBox newBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (isEmpty()) {
      return new BoundingBoxDoubleGf(geometryFactory);
    } else {
      final Iterable<Vertex> vertices = vertices();
      return new BoundingBoxDoubleGf(geometryFactory, vertices);
    }
  }

  @Override
  default Geometry newGeometry(final int axisCount, final double[] coordinates) {
    final GeometryFactory geometryFactory = convertAxisCount(axisCount);
    final int vertexCount = coordinates.length / axisCount;
    return newLineString(geometryFactory, axisCount, vertexCount, coordinates);
  }

  @Override
  default LineStringEditor newGeometryEditor() {
    return new LineStringEditor(this);
  }

  @Override
  default LineStringEditor newGeometryEditor(final int axisCount) {
    final LineStringEditor geometryEditor = newGeometryEditor();
    geometryEditor.setAxisCount(axisCount);
    return geometryEditor;
  }

  /**
   * Create a new {@link LinearRing} of this {@link LineString} using this geometry's geometry factory.
   *
   * @return The new linear ring.
   */
  default LineString newLinearRing() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.linearRing(this);
  }

  default LineString newLineString() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.lineString(this);
  }

  default LineString newLineString(final double... coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int axisCount = getAxisCount();
    final int vertexCount = coordinates.length / axisCount;
    return newLineString(geometryFactory, axisCount, vertexCount, coordinates);
  }

  default LineString newLineString(final GeometryFactory geometryFactory, final int axisCount,
    final int vertexCount, final double... coordinates) {
    final GeometryFactory geometryFactoryAxisCount = geometryFactory.convertAxisCount(axisCount);
    return geometryFactoryAxisCount.lineString(axisCount, vertexCount, coordinates);
  }

  default LineString newLineString(final int vertexCount, final double... coordinates) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    final int axisCount = getAxisCount();
    return newLineString(geometryFactory, axisCount, vertexCount, coordinates);
  }

  /**
   * Create a new {@link LineString} of this {@link LineString} using this geometry's geometry factory.
   *
   * @return The new line string.
   */
  default LineString newLineStringEmpty() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.lineString();
  }

  @SuppressWarnings("unchecked")
  @Override
  default <G> G newUsingGeometryFactory(final GeometryFactory factory) {
    if (factory == getGeometryFactory()) {
      return (G)this;
    } else if (isEmpty()) {
      return (G)newLineStringEmpty();
    } else {
      final double[] coordinates = getCoordinates();
      return (G)newLineString(coordinates);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G newValidGeometry() {
    if (isEmpty()) {
      return (G)this;
    } else if (isValid()) {
      return (G)normalize();
    } else {
      final LineMerger lines = new LineMerger(this);
      return (G)lines.getLineal();
    }
  }

  /**
   * Normalizes a LineString. A normalized linestring has the first point which
   * is not equal to it's reflected point less than the reflected point.
   */
  @Override
  default LineString normalize() {
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

  default int orientationIndex(final int index1, final int index2, final int index) {
    final double x1 = getX(index1);
    final double y1 = getY(index1);
    final double x2 = getX(index2);
    final double y2 = getY(index2);
    final double x = getX(index);
    final double y = getY(index);
    return CoordinatesListUtil.orientationIndex(x1, y1, x2, y2, x, y);
  }

  default Iterable<Point> points() {
    final List<Point> points = new ArrayList<>();
    for (int i = 0; i < getVertexCount(); i++) {
      final Point point = getPoint(i);
      points.add(point);
    }
    return points;
  }

  @Override
  @Deprecated
  default LineString prepare() {
    return new PreparedLineString(this);
  }

  @Override
  default LineString removeDuplicatePoints() {
    if (isEmpty()) {
      return this;
    } else {
      final int vertexCount = getVertexCount();
      if (vertexCount < 2) {
        return this;
      } else {
        final int axisCount = getAxisCount();
        final double[] coordinates = new double[vertexCount * axisCount];
        double previousX = getX(0);
        double previousY = getY(0);
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, 0, this, 0);
        int j = 1;
        for (int i = 1; i < vertexCount; i++) {
          final double x = getX(i);
          final double y = getY(i);
          if (x != previousX || y != previousY) {
            CoordinatesListUtil.setCoordinates(coordinates, axisCount, j++, this, i);
          }
          previousX = x;
          previousY = y;
        }

        if (j < 2) {
          return newLineStringEmpty();
        } else {
          return newLineString(j, coordinates);
        }
      }
    }
  }

  /**
   * Creates a {@link LineString} whose coordinates are in the reverse order of
   * this objects
   *
   * @return a {@link LineString} with coordinates in the reverse order
   */
  @Override
  default LineString reverse() {
    final int vertexCount = getVertexCount();
    final int axisCount = getAxisCount();
    final double[] coordinates = new double[vertexCount * axisCount];
    for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
      for (int axisIndex = 0; axisIndex < axisCount; axisIndex++) {
        final int coordinateIndex = (vertexCount - 1 - vertexIndex) * axisCount + axisIndex;
        coordinates[coordinateIndex] = getCoordinate(vertexIndex, axisIndex);
      }
    }
    final LineString reverseLine = newLineString(coordinates);
    GeometryProperties.copyUserData(this, reverseLine);
    return reverseLine;
  }

  @Override
  default Iterable<Segment> segments() {
    return new LineStringSegment(this, -1);
  }

  default List<LineString> split(Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    point = point.convertGeometry(geometryFactory);
    final Map<GeometryComponent, Double> result = LineStringUtil.findClosestGeometryComponent(this,
      point);
    if (result.isEmpty()) {
      return Collections.<LineString> singletonList(this);
    } else {
      final int vertexCount = getVertexCount();
      final GeometryComponent geometryComponent = CollectionUtil.get(result.keySet(), 0);
      final double distance = CollectionUtil.get(result.values(), 0);
      if (geometryComponent instanceof Vertex) {
        final Vertex vertex = (Vertex)geometryComponent;
        final int vertexIndex = vertex.getVertexIndex();
        if (distance == 0) {
          if (vertexIndex <= 0 || vertexIndex >= vertexCount - 1) {
            return Collections.<LineString> singletonList(this);
          } else {
            final LineString line1 = subLine(vertexIndex + 1);
            final LineString line2 = subLine(vertexIndex, vertexCount - vertexIndex);
            return Arrays.asList(line1, line2);
          }
        } else {
          final LineString line1 = subLine(vertexIndex + 1, point);
          final LineString line2 = subLine(point, vertexIndex, vertexCount - vertexIndex, null);
          return Arrays.asList(line1, line2);
        }
      } else if (geometryComponent instanceof Segment) {
        final Segment segment = (Segment)geometryComponent;
        final int segmentIndex = segment.getSegmentIndex();
        final LineString line1 = subLine(segmentIndex + 1, point);
        final LineString line2 = subLine(point, segmentIndex + 1, vertexCount - segmentIndex - 1,
          null);
        return Arrays.asList(line1, line2);
      } else {
        return Collections.<LineString> singletonList(this);
      }
    }
  }

  default LineString subLine(final int vertexCount) {
    return subLine(null, 0, vertexCount, null);
  }

  default LineString subLine(final int fromVertexIndex, final int vertexCount) {
    return subLine(null, fromVertexIndex, vertexCount, null);
  }

  default LineString subLine(final int vertexCount, final Point toPoint) {
    return subLine(null, 0, vertexCount, toPoint);
  }

  default LineString subLine(final Point fromPoint, final int fromVertexIndex, int vertexCount,
    final Point toPoint) {
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
    if (newVertexCount < 2) {
      return newLineStringEmpty();
    } else {
      final int axisCount = getAxisCount();
      final double[] coordinates = new double[newVertexCount * axisCount];
      int vertexIndex = 0;
      if (hasFromPoint) {
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexIndex++, fromPoint);
      }
      CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexIndex, this, fromVertexIndex,
        vertexCount);
      vertexIndex += vertexCount;
      if (hasToPoint) {
        CoordinatesListUtil.setCoordinates(coordinates, axisCount, vertexIndex++, toPoint);
      }
      final LineString newLine = newLineString(coordinates);
      GeometryProperties.copyUserData(this, newLine);
      return newLine;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toClockwise() {
    if (isClockwise()) {
      return (G)this;
    } else {
      return (G)this.reverse();
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  default <G extends Geometry> G toCounterClockwise() {
    if (isClockwise()) {
      return (G)this.reverse();
    } else {
      return (G)this;
    }
  }

  /**
   * Get the end of this line that touches an end of the other line. If the
   * lines don't touch at the ends then null will be returned.
   *
   * @return The end that touches.
   */
  default End touchingEnd(final LineString line) {
    if (isEmpty() || Property.isEmpty(line)) {
      return null;
    } else {
      for (final End end : End.VALUES) {
        final Point point = line.getPoint(end);
        final End touchingEnd = touchingEnd(point);
        if (touchingEnd != null) {
          return touchingEnd;
        }
      }
      return null;
    }
  }

  /**
   * Get the end of this line that touches the other point. If the point and
   * line don't touch at the end then null will be returned.
   *
   * @return The end that touches.
   */
  default End touchingEnd(final Point point) {
    if (isEmpty() || Property.isEmpty(point)) {
      return null;
    } else if (equalsVertex(0, point)) {
      return End.FROM;
    } else if (equalsVertex(-1, point)) {
      return End.TO;
    } else {
      return null;
    }
  }

  /**
   * Get the end of this line that touches an end of the other line. If the
   * lines don't touch at the ends then null will be returned.
   *
   * @return An array with the end of this line and then end of the other that
   *         touches, or null if they don't touch.
   */
  default End[] touchingEnds(final LineString line) {
    if (isEmpty() || Property.isEmpty(line)) {
      return null;
    } else {
      for (final End end : End.VALUES) {
        final Point point = line.getPoint(end);
        final End touchingEnd = touchingEnd(point);
        if (touchingEnd != null) {
          return new End[] {
            touchingEnd, end
          };
        }
      }
      return null;
    }
  }

  @Override
  default LineStringVertex vertices() {
    return new LineStringVertex(this, -1);
  }
}
