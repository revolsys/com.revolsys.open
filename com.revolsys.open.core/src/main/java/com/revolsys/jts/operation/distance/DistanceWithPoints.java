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
package com.revolsys.jts.operation.distance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.revolsys.jts.algorithm.PointLocator;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.segment.Segment;

/**
 * Find two points on two {@link Geometry}s which lie
 * within a given distance, or else are the nearest points
 * on the geometries (in which case this also
 * provides the distance between the geometries).
 * <p>
 * The distance computation also finds a pair of points in the input geometries
 * which have the minimum distance between them.
 * If a point lies in the interior of a line segment,
 * the coordinate computed is a close
 * approximation to the exact point.
 * <p>
 * The algorithms used are straightforward O(n^2)
 * comparisons.  This worst-case performance could be improved on
 * by using Voronoi techniques or spatial indexes.
 *
 * @version 1.7
 */
public class DistanceWithPoints {

  /**
   * Compute the distance between the nearest points of two geometries.
   * @param geometry1 a {@link Geometry}
   * @param geometry2 another {@link Geometry}
   * @return the distance between the geometries
   */
  public static double distance(final Geometry geometry1,
    final Geometry geometry2) {
    final DistanceWithPoints distOp = new DistanceWithPoints(geometry1,
      geometry2);
    return distOp.distance();
  }

  /**
   * Test whether two geometries lie within a given distance of each other.
   * @param geometry1 a {@link Geometry}
   * @param geometry2 another {@link Geometry}
   * @param distance the distance to test
   * @return true if geometry1.distance(geometry2) <= distance
   */
  public static boolean isWithinDistance(final Geometry geometry1,
    final Geometry geometry2, final double distance) {
    final DistanceWithPoints distOp = new DistanceWithPoints(geometry1,
      geometry2, distance);
    return distOp.distance() <= distance;
  }

  /**
   * Compute the the nearest points of two geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @param geometry1 a {@link Geometry}
   * @param geometry2 another {@link Geometry}
   * @return the nearest points in the geometries
   */
  public static List<Point> nearestPoints(final Geometry geometry1,
    final Geometry geometry2) {
    final DistanceWithPoints distOp = new DistanceWithPoints(geometry1,
      geometry2);
    return distOp.nearestPoints();
  }

  private final Geometry geometry1;

  private final Geometry geometry2;

  private double terminateDistance = 0.0;

  // working
  private final PointLocator pointLocator = new PointLocator();

  private Point minDistancePoint1;

  private Point minDistancePoint2;

  private double minDistance = Double.MAX_VALUE;

  private boolean computed = false;

  /**
   * Constructs a DistanceWithPoints that computes the distance and nearest points between
   * the two specified geometries.
   * @param geometry1 a Geometry
   * @param geometry2 a Geometry
   */
  public DistanceWithPoints(final Geometry geometry1, final Geometry geometry2) {
    this(geometry1, geometry2, 0.0);
  }

  /**
   * Constructs a DistanceWithPoints that computes the distance and nearest points between
   * the two specified geometries.
   * @param geometry1 a Geometry
   * @param geometry2 a Geometry
   * @param terminateDistance the distance on which to terminate the search
   */
  public DistanceWithPoints(final Geometry geometry1, final Geometry geometry2,
    final double terminateDistance) {
    if (geometry1 == null || geometry2 == null) {
      throw new IllegalArgumentException("null geometries are not supported");
    }
    this.geometry1 = geometry1;
    this.geometry2 = geometry2;
    this.terminateDistance = terminateDistance;
  }

  private boolean computeContainmentDistance() {
    if (computeContainmentDistance(geometry1, geometry2)) {
      return true;
    } else if (computeContainmentDistance(geometry2, geometry1)) {
      return true;
    } else {
      return false;
    }
  }

  private boolean computeContainmentDistance(final Geometry geometry1,
    final Geometry geometry2) {
    final List<Polygon> polygons = geometry1.getGeometries(Polygon.class);
    if (polygons.size() > 0) {
      final List<Point> insidePoints = ConnectedElementLocationFilter.getPoints(geometry2);
      if (computeContainmentDistance(insidePoints, polygons)) {
        return true;
      }
    }
    return false;
  }

  private boolean computeContainmentDistance(final List<Point> locations,
    final List<Polygon> polygons) {
    for (final Point insidePoint : locations) {
      for (final Polygon polygon : polygons) {
        if (computeContainmentDistance(insidePoint, polygon)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean computeContainmentDistance(final Point point,
    final Polygon poly) {
    // if point is not in exterior, distance to geom is 0
    if (Location.EXTERIOR != poly.locate(point)) {
      minDistance = 0.0;
      minDistancePoint1 = point;
      minDistancePoint2 = point;
      return true;
    } else {
      return false;
    }
  }

  /**
   * Computes distance between facets (lines and points)
   * of input geometries.
   *
   */
  private void computeFacetDistance() {
    /**
     * Geometries are not wholly inside, so compute distance from lines and points
     * of one to lines and points of the other
     */
    final List<LineString> lines0 = geometry1.getGeometryComponents(LineString.class);
    final List<LineString> lines1 = geometry2.getGeometryComponents(LineString.class);

    if (!computeLinesLines(lines0, lines1)) {
      final List<Point> points1 = geometry2.getGeometries(Point.class);
      if (!computeLinesPoints(lines0, points1)) {
        final List<Point> points0 = geometry1.getGeometries(Point.class);
        if (!computePointsLines(points0, lines1)) {
          computePointsPoints(points0, points1);
        }
      }
    }
  }

  private boolean computeLineLine(final LineString line1, final LineString line2) {
    if (minDistance == Double.MAX_VALUE
      || line1.getBoundingBox().distance(line2.getBoundingBox()) <= minDistance) {
      for (final Segment segment1 : line1.segments()) {
        for (final Segment segment2 : line2.segments()) {
          final double dist = segment1.distance(segment2);
          if (dist < minDistance) {
            minDistance = dist;
            final Point[] closestPt = segment1.closestPoints(segment2);
            minDistancePoint1 = closestPt[0];
            minDistancePoint2 = closestPt[1];
            if (minDistance <= terminateDistance) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean computeLinePoint(final LineString line, final Point point) {
    if (minDistance == Double.MAX_VALUE
      || line.getBoundingBox().distance(point) <= minDistance) {
      for (final Segment segment : line.segments()) {
        final double distance = segment.distance(point);
        if (distance < minDistance) {
          minDistance = distance;
          final Point closestPoint = segment.closestPoint(point);
          minDistancePoint1 = closestPoint;
          minDistancePoint2 = point;
          if (minDistance <= terminateDistance) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean computeLinesLines(final List<LineString> lines1,
    final List<LineString> lines2) {
    for (final LineString line1 : lines1) {
      for (final LineString line2 : lines2) {
        if (computeLineLine(line1, line2)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean computeLinesPoints(final List<LineString> lines,
    final List<Point> points) {
    for (final LineString line : lines) {
      for (final Point point : points) {
        if (computeLinePoint(line, point)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean computePointLine(final Point point, final LineString line) {
    final BoundingBox boundingBox = line.getBoundingBox();
    if (minDistance == Double.MAX_VALUE
      || boundingBox.distance(point) <= minDistance) {
      for (final Segment segment : line.segments()) {
        final double distance = segment.distance(point);
        if (distance < minDistance) {
          minDistance = distance;
          final Point closestPoint = segment.closestPoint(point);
          minDistancePoint1 = point;
          minDistancePoint2 = closestPoint;
          if (minDistance <= terminateDistance) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private boolean computePointsLines(final List<Point> points,
    final List<LineString> lines) {
    for (final Point point : points) {
      for (final LineString line : lines) {
        if (computePointLine(point, line)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean computePointsPoints(final List<Point> points1,
    final List<Point> points2) {
    for (final Point point1 : points1) {
      for (final Point point2 : points2) {
        final double dist = point1.distance(point2);
        if (dist < minDistance) {
          minDistance = dist;
          minDistancePoint1 = point1;
          minDistancePoint2 = point2;
          if (minDistance <= terminateDistance) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Report the distance between the nearest points on the input geometries.
   *
   * @return the distance between the geometries
   * or 0 if either input geometry is empty
   * @throws IllegalArgumentException if either input geometry is null
   */
  public double distance() {
    if (!computed) {
      computed = true;
      if (geometry1.isEmpty() || geometry2.isEmpty()) {
        minDistance = 0;
      } else {
        if (!computeContainmentDistance()) {
          computeFacetDistance();
        }
      }
    }
    return minDistance;
  }

  /**
   * Report the coordinates of the nearest points in the input geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @return a pair of {@link Coordinates}s of the nearest points
   */
  public List<Point> nearestPoints() {
    distance();
    if (minDistancePoint1 == null) {
      return Collections.emptyList();
    } else {
      return Arrays.asList(minDistancePoint1, minDistancePoint2);
    }
  }
}
