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
package com.revolsys.jts.geom;

import java.util.List;

import com.revolsys.jts.geom.metrics.PointLineStringMetrics;
import com.revolsys.jts.geom.segment.LineSegmentDouble;
import com.revolsys.jts.geom.segment.Segment;

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
public interface LineString extends Lineal {
  /**
   * Creates and returns a full copy of this {@link LineString} object.
   * (including all coordinates contained by it).
   *
   * @return a clone of this instance
   */

  @Override
  LineString clone();

  int copyCoordinates(int axisCount, double nanValue, double[] destCoordinates, int destOffset);

  int copyCoordinatesReverse(int axisCount, double nanValue, double[] destCoordinates,
    int destOffset);

  double distance(int index, Point point);

  double distance(Point point);

  double distance(Point point, double terminateDistance);

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

  boolean equals(int axisIndex, int vertexIndex, Point point);

  boolean equalsVertex(final int vertexIndex, final double... coordinates);

  boolean equalsVertex(int axisCount, final int vertexIndex1, final int vertexIndex2);

  boolean equalsVertex(int axisCount, final int vertexIndex, final LineString line2,
    int vertexIndex2);

  boolean equalsVertex(int axisCount, final int vertexIndex, final Point point);

  boolean equalsVertex(final int vertexIndex, final Point point);

  double getCoordinate(int vertexIndex, final int axisIndex);

  double[] getCoordinates();

  double[] getCoordinates(int axisCount);

  double[] getCoordinates(int axisCount, double nanValue);

  LineString getCoordinatesList();

  Point getFromPoint();

  double getM(int vertexIndex);

  default PointLineStringMetrics getMetrics(Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    point = point.convert(geometryFactory, 2);
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

  Point getPoint(final int vertexIndex);

  default Point getPoint(final End lineEnd) {
    if (End.isFrom(lineEnd)) {
      return getFromPoint();
    } else {
      return getToPoint();
    }
  }

  int getSegmentCount();

  default Side getSide(Point point) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    point = point.convert(geometryFactory, 2);
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

  Point getToPoint();

  double getX(int vertexIndex);

  double getY(int vertexIndex);

  double getZ(int vertexIndex);

  boolean hasVertex(Point point);

  boolean isClockwise();

  boolean isClosed();

  boolean isCounterClockwise();

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

  boolean isRing();

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
  LineString merge(LineString line);

  LineString merge(Point point, LineString line);

  @Override
  LineString move(final double... deltas);

  LineString moveVertex(Point newPoint, int vertexIndex);

  /**
   * Normalizes a LineString.  A normalized linestring
   * has the first point which is not equal to it's reflected point
   * less than the reflected point.
   */

  @Override
  LineString normalize();

  Iterable<Point> points();

  @Override
  LineString prepare();

  @Override
  LineString reverse();

  List<LineString> split(Point point);

  LineString subLine(final int vertexCount);

  LineString subLine(final int fromVertexIndex, int vertexCount);

  LineString subLine(final int vertexCount, final Point toPoint);

  LineString subLine(final Point fromPoint, final int fromVertexIndex, int vertexCount,
    final Point toPoint);
}
