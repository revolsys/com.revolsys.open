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
package com.revolsys.geometry.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import com.revolsys.geometry.model.CoordinateArrays;
import com.revolsys.geometry.model.CoordinateList;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.util.Assert;
import com.revolsys.geometry.util.UniqueCoordinateArrayFilter;

/**
 * Computes the convex hull of a {@link Geometry}.
 * The convex hull is the smallest convex Geometry that contains all the
 * points in the input Geometry.
 * <p>
 * Uses the Graham Scan algorithm.
 *
 *@version 1.7
 */
public class ConvexHull {
  /**
   * Compares {@link Coordinates}s for their angle and distance
   * relative to an origin.
   *
   * @author Martin Davis
   * @version 1.7
   */
  private static class RadialComparator implements Comparator<Point> {
    /**
     * Given two points p and q compare them with respect to their radial
     * ordering about point o.  First checks radial ordering.
     * If points are collinear, the comparison is based
     * on their distance to the origin.
     * <p>
     * p < q iff
     * <ul>
     * <li>ang(o-p) < ang(o-q) (e.g. o-p-q is CCW)
     * <li>or ang(o-p) == ang(o-q) && dist(o,p) < dist(o,q)
     * </ul>
     *
     * @param o the origin
     * @param p a point
     * @param q another point
     * @return -1, 0 or 1 depending on whether p is less than,
     * equal to or greater than q
     */
    private static int polarCompare(final Point o, final Point p, final Point q) {
      final double dxp = p.getX() - o.getX();
      final double dyp = p.getY() - o.getY();
      final double dxq = q.getX() - o.getX();
      final double dyq = q.getY() - o.getY();

      /*
       * // MD - non-robust int result = 0; double alph = Math.atan2(dxp, dyp); double beta =
       * Math.atan2(dxq, dyq); if (alph < beta) { result = -1; } if (alph > beta) { result = 1; } if
       * (result != 0) return result; //
       */

      final int orient = CGAlgorithmsDD.orientationIndex(o, p, q);

      if (orient == CGAlgorithms.COUNTERCLOCKWISE) {
        return 1;
      }
      if (orient == CGAlgorithms.CLOCKWISE) {
        return -1;
      }

      // points are collinear - check distance
      final double op = dxp * dxp + dyp * dyp;
      final double oq = dxq * dxq + dyq * dyq;
      if (op < oq) {
        return -1;
      }
      if (op > oq) {
        return 1;
      }
      return 0;
    }

    private final Point origin;

    public RadialComparator(final Point origin) {
      this.origin = origin;
    }

    @Override
    public int compare(final Point p1, final Point p2) {
      return polarCompare(this.origin, p1, p2);
    }

  }

  /**
   * Tests whether a point lies inside or on a ring. The ring may be oriented in
   * either direction. A point lying exactly on the ring boundary is considered
   * to be inside the ring.
   * <p>
   * This method does <i>not</i> first check the point against the envelope of
   * the ring.
   *
   * @param p
   *          point to check for ring inclusion
   * @param ring
   *          an array of coordinates representing the ring (which must have
   *          first point identical to last point)
   * @return true if p is inside ring
   *
   * @see locatePointInRing
   */
  public static boolean isPointInRing(final Point p, final Point... ring) {
    final Location location = RayCrossingCounter.locatePointInRing(p, ring);
    return location != Location.EXTERIOR;
  }

  private final GeometryFactory geomFactory;

  private final Point[] inputPts;

  /**
   * Construct a new new convex hull construction for the input {@link Geometry}.
   */
  public ConvexHull(final Geometry geometry) {
    this(UniqueCoordinateArrayFilter.getUniquePointsArray(geometry), geometry.getGeometryFactory());
  }

  /**
   * Construct a new new convex hull construction for the input {@link Coordinates} array.
   */
  public ConvexHull(final Point[] pts, final GeometryFactory geomFactory) {
    this.inputPts = UniqueCoordinateArrayFilter.getUniquePointsArray(Arrays.asList(pts));
    // inputPts = pts;
    this.geomFactory = geomFactory;
  }

  /**
   *@param  vertices  the vertices of a linear ring, which may or may not be
   *      flattened (i.e. vertices collinear)
   *@return           the coordinates with unnecessary (collinear) vertices
   *      removed
   */
  private Point[] cleanRing(final Point[] original) {
    Assert.equals(original[0], original[original.length - 1]);
    final List<Point> cleanedRing = new ArrayList<>();
    Point previousDistinctCoordinate = null;
    for (int i = 0; i <= original.length - 2; i++) {
      final Point currentCoordinate = original[i];
      final Point nextCoordinate = original[i + 1];
      if (currentCoordinate.equals(nextCoordinate)) {
        continue;
      }
      if (previousDistinctCoordinate != null
        && isBetween(previousDistinctCoordinate, currentCoordinate, nextCoordinate)) {
        continue;
      }
      cleanedRing.add(currentCoordinate);
      previousDistinctCoordinate = currentCoordinate;
    }
    cleanedRing.add(original[original.length - 1]);
    final Point[] cleanedRingCoordinates = new Point[cleanedRing.size()];
    return cleanedRing.toArray(cleanedRingCoordinates);
  }

  private Point[] computeOctPts(final Point[] inputPts) {
    final Point[] pts = new Point[8];
    for (int j = 0; j < pts.length; j++) {
      pts[j] = inputPts[0];
    }
    for (int i = 1; i < inputPts.length; i++) {
      if (inputPts[i].getX() < pts[0].getX()) {
        pts[0] = inputPts[i];
      }
      if (inputPts[i].getX() - inputPts[i].getY() < pts[1].getX() - pts[1].getY()) {
        pts[1] = inputPts[i];
      }
      if (inputPts[i].getY() > pts[2].getY()) {
        pts[2] = inputPts[i];
      }
      if (inputPts[i].getX() + inputPts[i].getY() > pts[3].getX() + pts[3].getY()) {
        pts[3] = inputPts[i];
      }
      if (inputPts[i].getX() > pts[4].getX()) {
        pts[4] = inputPts[i];
      }
      if (inputPts[i].getX() - inputPts[i].getY() > pts[5].getX() - pts[5].getY()) {
        pts[5] = inputPts[i];
      }
      if (inputPts[i].getY() < pts[6].getY()) {
        pts[6] = inputPts[i];
      }
      if (inputPts[i].getX() + inputPts[i].getY() < pts[7].getX() + pts[7].getY()) {
        pts[7] = inputPts[i];
      }
    }
    return pts;

  }

  private Point[] computeOctRing(final Point[] inputPts) {
    final Point[] octPts = computeOctPts(inputPts);
    final CoordinateList coordList = new CoordinateList();
    coordList.add(octPts, false);

    // points must all lie in a line
    if (coordList.size() < 3) {
      return null;
    }
    coordList.closeRing();
    return coordList.toCoordinateArray();
  }

  /**
   * Returns a {@link Geometry} that represents the convex hull of the input
   * geometry.
   * The returned geometry contains the minimal number of points needed to
   * represent the convex hull.  In particular, no more than two consecutive
   * points will be collinear.
   *
   * @return if the convex hull contains 3 or more points, a {@link Polygon};
   * 2 points, a {@link LineString};
   * 1 point, a {@link Point};
   * 0 points, an empty {@link Geometry}.
   */
  public Geometry getConvexHull() {

    if (this.inputPts.length == 0) {
      return this.geomFactory.geometryCollection();
    }
    if (this.inputPts.length == 1) {
      return this.geomFactory.point(this.inputPts[0]);
    }
    if (this.inputPts.length == 2) {
      return this.geomFactory.lineString(this.inputPts);
    }

    Point[] reducedPts = this.inputPts;
    // use heuristic to reduce points, if large
    if (this.inputPts.length > 50) {
      reducedPts = reduce(this.inputPts);
    }
    // sort points for Graham scan.
    final Point[] sortedPts = preSort(reducedPts);

    // Use Graham scan to find convex hull.
    final Stack<Point> cHS = grahamScan(sortedPts);

    // Convert stack to an array.
    final Point[] cH = toCoordinateArray(cHS);

    // Convert array to appropriate output geometry.
    return lineOrPolygon(cH);
  }

  /**
   * Uses the Graham Scan algorithm to compute the convex hull vertices.
   *
   * @param c a list of points, with at least 3 entries
   * @return a Stack containing the ordered points of the convex hull ring
   */
  private Stack<Point> grahamScan(final Point[] c) {
    Point p;
    final Stack<Point> ps = new Stack<>();
    p = ps.push(c[0]);
    p = ps.push(c[1]);
    p = ps.push(c[2]);
    for (int i = 3; i < c.length; i++) {
      p = ps.pop();
      // check for empty stack to guard against robustness problems
      while (!ps.empty() && CGAlgorithmsDD.orientationIndex(ps.peek(), p, c[i]) > 0) {
        p = ps.pop();
      }
      p = ps.push(p);
      p = ps.push(c[i]);
    }
    p = ps.push(c[0]);
    return ps;
  }

  /**
   *@return    whether the three coordinates are collinear and c2 lies between
   *      c1 and c3 inclusive
   */
  private boolean isBetween(final Point c1, final Point c2, final Point c3) {
    if (CGAlgorithmsDD.orientationIndex(c1, c2, c3) != 0) {
      return false;
    }
    if (c1.getX() != c3.getX()) {
      if (c1.getX() <= c2.getX() && c2.getX() <= c3.getX()) {
        return true;
      }
      if (c3.getX() <= c2.getX() && c2.getX() <= c1.getX()) {
        return true;
      }
    }
    if (c1.getY() != c3.getY()) {
      if (c1.getY() <= c2.getY() && c2.getY() <= c3.getY()) {
        return true;
      }
      if (c3.getY() <= c2.getY() && c2.getY() <= c1.getY()) {
        return true;
      }
    }
    return false;
  }

  /**
   *@param  vertices  the vertices of a linear ring, which may or may not be
   *      flattened (i.e. vertices collinear)
   *@return           a 2-vertex <code>LineString</code> if the vertices are
   *      collinear; otherwise, a <code>Polygon</code> with unnecessary
   *      (collinear) vertices removed
   */
  private Geometry lineOrPolygon(Point[] coordinates) {

    coordinates = cleanRing(coordinates);
    if (coordinates.length == 3) {
      return this.geomFactory.lineString(coordinates[0], coordinates[1]);
    }
    final LinearRing ring = this.geomFactory.linearRing(coordinates);
    return this.geomFactory.polygon(ring);
  }

  /*
   * // MD - no longer used, but keep for reference purposes private Point[] computeQuad(Point[]
   * inputPts) { BigQuad bigQuad = bigQuad(inputPts); // Build a linear ring defining a big poly.
   * ArrayList bigPoly = new ArrayList(); bigPoly.add(bigQuad.westmost); if (!
   * bigPoly.contains(bigQuad.northmost)) { bigPoly.add(bigQuad.northmost); } if (!
   * bigPoly.contains(bigQuad.eastmost)) { bigPoly.add(bigQuad.eastmost); } if (!
   * bigPoly.contains(bigQuad.southmost)) { bigPoly.add(bigQuad.southmost); } // points must all lie
   * in a line if (bigPoly.size() < 3) { return null; } // closing point
   * bigPoly.add(bigQuad.westmost); Point[] bigPolyArray =
   * CoordinateArrays.toCoordinateArray(bigPoly); return bigPolyArray; } private BigQuad
   * bigQuad(Point[] pts) { BigQuad bigQuad = new BigQuad(); bigQuad.northmost = pts[0];
   * bigQuad.southmost = pts[0]; bigQuad.westmost = pts[0]; bigQuad.eastmost = pts[0]; for (int i =
   * 1; i < pts.length; i++) { if (pts[i].x < bigQuad.westmost.x) { bigQuad.westmost = pts[i]; } if
   * (pts[i].x > bigQuad.eastmost.x) { bigQuad.eastmost = pts[i]; } if (pts[i].y <
   * bigQuad.southmost.y) { bigQuad.southmost = pts[i]; } if (pts[i].y > bigQuad.northmost.y) {
   * bigQuad.northmost = pts[i]; } } return bigQuad; } private static class BigQuad { public Point
   * northmost; public Point southmost; public Point westmost; public Coordinate eastmost; }
   */

  private Point[] padArray3(final Point[] pts) {
    final Point[] pad = new Point[3];
    for (int i = 0; i < pad.length; i++) {
      if (i < pts.length) {
        pad[i] = pts[i];
      } else {
        pad[i] = pts[0];
      }
    }
    return pad;
  }

  private Point[] preSort(final Point[] pts) {
    Point t;

    // find the lowest point in the set. If two or more points have
    // the same minimum y coordinate choose the one with the minimu x.
    // This focal point is put in array location pts[0].
    for (int i = 1; i < pts.length; i++) {
      if (pts[i].getY() < pts[0].getY()
        || pts[i].getY() == pts[0].getY() && pts[i].getX() < pts[0].getX()) {
        t = pts[0];
        pts[0] = pts[i];
        pts[i] = t;
      }
    }

    // sort the points radially around the focal point.
    Arrays.sort(pts, 1, pts.length, new RadialComparator(pts[0]));

    // radialSort(pts);
    return pts;
  }

  /**
   * Uses a heuristic to reduce the number of points scanned
   * to compute the hull.
   * The heuristic is to find a polygon guaranteed to
   * be in (or on) the hull, and eliminate all points inside it.
   * A quadrilateral defined by the extremal points
   * in the four orthogonal directions
   * can be used, but even more inclusive is
   * to use an octilateral defined by the points in the 8 cardinal directions.
   * <p>
   * Note that even if the method used to determine the polygon vertices
   * is not 100% robust, this does not affect the robustness of the convex hull.
   * <p>
   * To satisfy the requirements of the Graham Scan algorithm,
   * the returned array has at least 3 entries.
   *
   * @param pts the points to reduce
   * @return the reduced list of points (at least 3)
   */
  private Point[] reduce(final Point[] inputPts) {
    // Point[] polyPts = computeQuad(inputPts);
    final Point[] polyPts = computeOctRing(inputPts);
    // Point[] polyPts = null;

    // unable to compute interior polygon for some reason
    if (polyPts == null) {
      return inputPts;
    }

    // LinearRing ring = geomFactory.createLinearRing(polyPts);
    // System.out.println(ring);

    // add points defining polygon
    final Set<Point> reducedSet = new TreeSet<>();
    for (final Point polyPt : polyPts) {
      reducedSet.add(polyPt);
    }
    /**
     * Add all unique points not in the interior poly.
     * CGAlgorithms.isPointInRing is not defined for points actually on the ring,
     * but this doesn't matter since the points of the interior polygon
     * are forced to be in the reduced set.
     */
    for (int i = 0; i < inputPts.length; i++) {
      if (!ConvexHull.isPointInRing(inputPts[i], polyPts)) {
        reducedSet.add(inputPts[i]);
      }
    }
    final Point[] reducedPts = CoordinateArrays.toCoordinateArray(reducedSet);

    // ensure that computed array has at least 3 points (not necessarily unique)
    if (reducedPts.length < 3) {
      return padArray3(reducedPts);
    }
    return reducedPts;
  }

  /**
   * An alternative to Stack.toArray, which is not present in earlier versions
   * of Java.
   */
  protected Point[] toCoordinateArray(final Stack<Point> stack) {
    final Point[] coordinates = new Point[stack.size()];
    for (int i = 0; i < stack.size(); i++) {
      final Point coordinate = stack.get(i);
      coordinates[i] = coordinate;
    }
    return coordinates;
  }
}
