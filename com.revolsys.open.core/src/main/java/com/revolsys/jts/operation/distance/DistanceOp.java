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

import java.util.List;

import com.revolsys.jts.algorithm.PointLocator;
import com.revolsys.jts.geom.Coordinates;
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
public class DistanceOp {
  /**
   * Compute the the closest points of two geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @param g0 a {@link Geometry}
   * @param g1 another {@link Geometry}
   * @return the closest points in the geometries
   * @deprecated renamed to nearestPoints
   */
  @Deprecated
  public static Coordinates[] closestPoints(final Geometry g0, final Geometry g1) {
    final DistanceOp distOp = new DistanceOp(g0, g1);
    return distOp.nearestPoints();
  }

  /**
   * Compute the distance between the nearest points of two geometries.
   * @param g0 a {@link Geometry}
   * @param g1 another {@link Geometry}
   * @return the distance between the geometries
   */
  public static double distance(final Geometry g0, final Geometry g1) {
    final DistanceOp distOp = new DistanceOp(g0, g1);
    return distOp.distance();
  }

  /**
   * Test whether two geometries lie within a given distance of each other.
   * @param g0 a {@link Geometry}
   * @param g1 another {@link Geometry}
   * @param distance the distance to test
   * @return true if g0.distance(g1) <= distance
   */
  public static boolean isWithinDistance(final Geometry g0, final Geometry g1,
    final double distance) {
    final DistanceOp distOp = new DistanceOp(g0, g1, distance);
    return distOp.distance() <= distance;
  }

  /**
   * Compute the the nearest points of two geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @param g0 a {@link Geometry}
   * @param g1 another {@link Geometry}
   * @return the nearest points in the geometries
   */
  public static Coordinates[] nearestPoints(final Geometry g0, final Geometry g1) {
    final DistanceOp distOp = new DistanceOp(g0, g1);
    return distOp.nearestPoints();
  }

  // input
  private final Geometry[] geom;

  private double terminateDistance = 0.0;

  // working
  private final PointLocator ptLocator = new PointLocator();

  private GeometryLocation[] minDistanceLocation;

  private double minDistance = Double.MAX_VALUE;

  /**
   * Constructs a DistanceOp that computes the distance and nearest points between
   * the two specified geometries.
   * @param g0 a Geometry
   * @param g1 a Geometry
   */
  public DistanceOp(final Geometry g0, final Geometry g1) {
    this(g0, g1, 0.0);
  }

  /**
   * Constructs a DistanceOp that computes the distance and nearest points between
   * the two specified geometries.
   * @param g0 a Geometry
   * @param g1 a Geometry
   * @param terminateDistance the distance on which to terminate the search
   */
  public DistanceOp(final Geometry g0, final Geometry g1,
    final double terminateDistance) {
    this.geom = new Geometry[2];
    geom[0] = g0;
    geom[1] = g1;
    this.terminateDistance = terminateDistance;
  }

  private void computeContainmentDistance() {
    final GeometryLocation[] locPtPoly = new GeometryLocation[2];
    // test if either geometry has a vertex inside the other
    computeContainmentDistance(0, locPtPoly);
    if (minDistance > terminateDistance) {
      computeContainmentDistance(1, locPtPoly);
    }
  }

  private void computeContainmentDistance(final GeometryLocation ptLoc,
    final Polygon poly, final GeometryLocation[] locPtPoly) {
    final Coordinates pt = ptLoc.getCoordinate();
    // if pt is not in exterior, distance to geom is 0
    if (Location.EXTERIOR != ptLocator.locate(pt, poly)) {
      minDistance = 0.0;
      locPtPoly[0] = ptLoc;
      locPtPoly[1] = new GeometryLocation(poly, pt);
    }
  }

  private void computeContainmentDistance(final int polyGeomIndex,
    final GeometryLocation[] locPtPoly) {
    final int locationsIndex = 1 - polyGeomIndex;
    final List<Polygon> polys = geom[polyGeomIndex].getGeometries(Polygon.class);
    if (polys.size() > 0) {
      final List<GeometryLocation> insideLocs = ConnectedElementLocationFilter.getLocations(geom[locationsIndex]);
      computeContainmentDistance(insideLocs, polys, locPtPoly);
      if (minDistance <= terminateDistance) {
        // this assigment is determined by the order of the args in the
        // computeInside call above
        minDistanceLocation[locationsIndex] = locPtPoly[0];
        minDistanceLocation[polyGeomIndex] = locPtPoly[1];
        return;
      }
    }
  }

  private void computeContainmentDistance(
    final List<GeometryLocation> locations, final List<Polygon> polygons,
    final GeometryLocation[] locPtPoly) {
    for (final GeometryLocation loc : locations) {
      for (final Polygon polygon : polygons) {
        computeContainmentDistance(loc, polygon, locPtPoly);
        if (minDistance <= terminateDistance) {
          return;
        }
      }
    }
  }

  /**
   * Computes distance between facets (lines and points)
   * of input geometries.
   *
   */
  private void computeFacetDistance() {
    final GeometryLocation[] locGeom = new GeometryLocation[2];

    /**
     * Geometries are not wholely inside, so compute distance from lines and points
     * of one to lines and points of the other
     */
    final List<LineString> lines0 = geom[0].getGeometryComponents(LineString.class);
    final List<LineString> lines1 = geom[1].getGeometryComponents(LineString.class);

    final List<Point> pts0 = geom[0].getGeometries(Point.class);
    final List<Point> pts1 = geom[1].getGeometries(Point.class);

    // exit whenever minDistance goes LE than terminateDistance
    computeMinDistanceLines(lines0, lines1, locGeom);
    updateMinDistance(locGeom, false);
    if (minDistance <= terminateDistance) {
      return;
    }

    locGeom[0] = null;
    locGeom[1] = null;
    computeMinDistanceLinesPoints(lines0, pts1, locGeom);
    updateMinDistance(locGeom, false);
    if (minDistance <= terminateDistance) {
      return;
    }

    locGeom[0] = null;
    locGeom[1] = null;
    computeMinDistanceLinesPoints(lines1, pts0, locGeom);
    updateMinDistance(locGeom, true);
    if (minDistance <= terminateDistance) {
      return;
    }

    locGeom[0] = null;
    locGeom[1] = null;
    computeMinDistancePoints(pts0, pts1, locGeom);
    updateMinDistance(locGeom, false);
  }

  private void computeMinDistance() {
    // only compute once!
    if (minDistanceLocation != null) {
      return;
    }

    minDistanceLocation = new GeometryLocation[2];
    computeContainmentDistance();
    if (minDistance <= terminateDistance) {
      return;
    }
    computeFacetDistance();
  }

  private void computeMinDistance(final LineString line1,
    final LineString line2, final GeometryLocation[] locGeom) {
    if (line1.getBoundingBox().distance(line2.getBoundingBox()) <= minDistance) {
      // brute force approach!
      int i = 0;
      for (final Segment segment1 : line1.segments()) {
        int j = 0;
        for (final Segment segment2 : line2.segments()) {
          final double dist = segment1.distance(segment2);
          if (dist < minDistance) {
            minDistance = dist;
            final Coordinates[] closestPt = segment1.closestPoints(segment2);
            locGeom[0] = new GeometryLocation(line1, i,
              closestPt[0].cloneCoordinates());
            locGeom[1] = new GeometryLocation(line2, j,
              closestPt[1].cloneCoordinates());
          }
          if (minDistance <= terminateDistance) {
            return;
          }
          j++;
        }
        i++;
      }
    }
  }

  private void computeMinDistance(final LineString line, final Point point,
    final GeometryLocation[] locGeom) {
    if (line.getBoundingBox().distance(point.getBoundingBox()) > minDistance) {
      return;
    }
    // brute force approach!
    int i = 0;
    for (final Segment segment : line.segments()) {
      final double distance = segment.distance(point);
      if (distance < minDistance) {
        minDistance = distance;
        final Coordinates segClosestPoint = segment.closestPoint(point);
        locGeom[0] = new GeometryLocation(line, i,
          segClosestPoint.cloneCoordinates());
        locGeom[1] = new GeometryLocation(point, 0, point);
      }
      if (minDistance <= terminateDistance) {
        return;
      }
      i++;
    }
  }

  private void computeMinDistanceLines(final List<LineString> lines1,
    final List<LineString> lines2, final GeometryLocation[] locGeom) {
    for (final LineString line1 : lines1) {
      for (final LineString line2 : lines2) {
        computeMinDistance(line1, line2, locGeom);
        if (minDistance <= terminateDistance) {
          return;
        }
      }
    }
  }

  private void computeMinDistanceLinesPoints(final List<LineString> lines,
    final List<Point> points, final GeometryLocation[] locGeom) {
    for (final LineString line : lines) {
      for (final Point point : points) {
        computeMinDistance(line, point, locGeom);
        if (minDistance <= terminateDistance) {
          return;
        }
      }
    }
  }

  private void computeMinDistancePoints(final List<Point> points0,
    final List<Point> points1, final GeometryLocation[] locGeom) {
    for (final Point pt0 : points0) {
      for (final Point pt1 : points1) {
        final double dist = pt0.distance((Coordinates)pt1);
        if (dist < minDistance) {
          minDistance = dist;
          locGeom[0] = new GeometryLocation(pt0, 0, pt0);
          locGeom[1] = new GeometryLocation(pt1, 0, pt1);
        }
        if (minDistance <= terminateDistance) {
          return;
        }
      }
    }
  }

  /**
   * Report the distance between the nearest points on the input geometries.
   *
   * @return the distance between the geometries
   * or 0 if either input geometry is empty
   * @throws IllegalArgumentException if either input geometry is null
   */
  public double distance() {
    if (geom[0] == null || geom[1] == null) {
      throw new IllegalArgumentException("null geometries are not supported");
    }
    if (geom[0].isEmpty() || geom[1].isEmpty()) {
      return 0.0;
    }

    computeMinDistance();
    return minDistance;
  }

  /**
   * Report the locations of the nearest points in the input geometries.
   * The locations are presented in the same order as the input Geometries.
   *
   * @return a pair of {@link GeometryLocation}s for the nearest points
   */
  public GeometryLocation[] nearestLocations() {
    computeMinDistance();
    return minDistanceLocation;
  }

  /**
   * Report the coordinates of the nearest points in the input geometries.
   * The points are presented in the same order as the input Geometries.
   *
   * @return a pair of {@link Coordinates}s of the nearest points
   */
  public Coordinates[] nearestPoints() {
    computeMinDistance();
    final Coordinates[] nearestPts = new Coordinates[] {
      minDistanceLocation[0].getCoordinate(),
      minDistanceLocation[1].getCoordinate()
    };
    return nearestPts;
  }

  private void updateMinDistance(final GeometryLocation[] locGeom,
    final boolean flip) {
    // if not set then don't update
    if (locGeom[0] == null) {
      return;
    }

    if (flip) {
      minDistanceLocation[0] = locGeom[1];
      minDistanceLocation[1] = locGeom[0];
    } else {
      minDistanceLocation[0] = locGeom[0];
      minDistanceLocation[1] = locGeom[1];
    }
  }

}
