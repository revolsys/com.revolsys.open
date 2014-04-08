
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
package com.revolsys.jts.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;
import java.util.TreeSet;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.util.Assert;
import com.revolsys.jts.util.UniqueCoordinateArrayFilter;

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
   * Compares {@link Coordinate}s for their angle and distance
   * relative to an origin.
   *
   * @author Martin Davis
   * @version 1.7
   */
  private static class RadialComparator implements Comparator {
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
    private static int polarCompare(final Coordinate o, final Coordinate p,
      final Coordinate q) {
      final double dxp = p.x - o.x;
      final double dyp = p.y - o.y;
      final double dxq = q.x - o.x;
      final double dyq = q.y - o.y;

      /*
       * // MD - non-robust int result = 0; double alph = Math.atan2(dxp, dyp);
       * double beta = Math.atan2(dxq, dyq); if (alph < beta) { result = -1; }
       * if (alph > beta) { result = 1; } if (result != 0) return result; //
       */

      final int orient = CGAlgorithms.computeOrientation(o, p, q);

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

    private final Coordinate origin;

    public RadialComparator(final Coordinate origin) {
      this.origin = origin;
    }

    @Override
    public int compare(final Object o1, final Object o2) {
      final Coordinate p1 = (Coordinate)o1;
      final Coordinate p2 = (Coordinate)o2;
      return polarCompare(origin, p1, p2);
    }

  }

  private static Coordinate[] extractCoordinates(final Geometry geom) {
    final UniqueCoordinateArrayFilter filter = new UniqueCoordinateArrayFilter();
    geom.apply(filter);
    return filter.getCoordinates();
  }

  private final GeometryFactory geomFactory;

  private final Coordinate[] inputPts;

  /**
   * Create a new convex hull construction for the input {@link Coordinate} array.
   */
  public ConvexHull(final Coordinate[] pts, final GeometryFactory geomFactory) {
    inputPts = UniqueCoordinateArrayFilter.filterCoordinates(pts);
    // inputPts = pts;
    this.geomFactory = geomFactory;
  }

  /**
   * Create a new convex hull construction for the input {@link Geometry}.
   */
  public ConvexHull(final Geometry geometry) {
    this(extractCoordinates(geometry), geometry.getGeometryFactory());
  }

  /**
   *@param  vertices  the vertices of a linear ring, which may or may not be
   *      flattened (i.e. vertices collinear)
   *@return           the coordinates with unnecessary (collinear) vertices
   *      removed
   */
  private Coordinate[] cleanRing(final Coordinate[] original) {
    Assert.equals(original[0], original[original.length - 1]);
    final ArrayList cleanedRing = new ArrayList();
    Coordinate previousDistinctCoordinate = null;
    for (int i = 0; i <= original.length - 2; i++) {
      final Coordinate currentCoordinate = original[i];
      final Coordinate nextCoordinate = original[i + 1];
      if (currentCoordinate.equals(nextCoordinate)) {
        continue;
      }
      if (previousDistinctCoordinate != null
        && isBetween(previousDistinctCoordinate, currentCoordinate,
          nextCoordinate)) {
        continue;
      }
      cleanedRing.add(currentCoordinate);
      previousDistinctCoordinate = currentCoordinate;
    }
    cleanedRing.add(original[original.length - 1]);
    final Coordinate[] cleanedRingCoordinates = new Coordinate[cleanedRing.size()];
    return (Coordinate[])cleanedRing.toArray(cleanedRingCoordinates);
  }

  private Coordinate[] computeOctPts(final Coordinate[] inputPts) {
    final Coordinate[] pts = new Coordinate[8];
    for (int j = 0; j < pts.length; j++) {
      pts[j] = inputPts[0];
    }
    for (int i = 1; i < inputPts.length; i++) {
      if (inputPts[i].x < pts[0].x) {
        pts[0] = inputPts[i];
      }
      if (inputPts[i].x - inputPts[i].y < pts[1].x - pts[1].y) {
        pts[1] = inputPts[i];
      }
      if (inputPts[i].y > pts[2].y) {
        pts[2] = inputPts[i];
      }
      if (inputPts[i].x + inputPts[i].y > pts[3].x + pts[3].y) {
        pts[3] = inputPts[i];
      }
      if (inputPts[i].x > pts[4].x) {
        pts[4] = inputPts[i];
      }
      if (inputPts[i].x - inputPts[i].y > pts[5].x - pts[5].y) {
        pts[5] = inputPts[i];
      }
      if (inputPts[i].y < pts[6].y) {
        pts[6] = inputPts[i];
      }
      if (inputPts[i].x + inputPts[i].y < pts[7].x + pts[7].y) {
        pts[7] = inputPts[i];
      }
    }
    return pts;

  }

  private Coordinate[] computeOctRing(final Coordinate[] inputPts) {
    final Coordinate[] octPts = computeOctPts(inputPts);
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
   * 0 points, an empty {@link GeometryCollection}.
   */
  public Geometry getConvexHull() {

    if (inputPts.length == 0) {
      return geomFactory.createGeometryCollection();
    }
    if (inputPts.length == 1) {
      return geomFactory.createPoint(inputPts[0]);
    }
    if (inputPts.length == 2) {
      return geomFactory.createLineString(inputPts);
    }

    Coordinate[] reducedPts = inputPts;
    // use heuristic to reduce points, if large
    if (inputPts.length > 50) {
      reducedPts = reduce(inputPts);
    }
    // sort points for Graham scan.
    final Coordinate[] sortedPts = preSort(reducedPts);

    // Use Graham scan to find convex hull.
    final Stack cHS = grahamScan(sortedPts);

    // Convert stack to an array.
    final Coordinate[] cH = toCoordinateArray(cHS);

    // Convert array to appropriate output geometry.
    return lineOrPolygon(cH);
  }

  /**
   * Uses the Graham Scan algorithm to compute the convex hull vertices.
   * 
   * @param c a list of points, with at least 3 entries
   * @return a Stack containing the ordered points of the convex hull ring
   */
  private Stack grahamScan(final Coordinate[] c) {
    Coordinate p;
    final Stack ps = new Stack();
    p = (Coordinate)ps.push(c[0]);
    p = (Coordinate)ps.push(c[1]);
    p = (Coordinate)ps.push(c[2]);
    for (int i = 3; i < c.length; i++) {
      p = (Coordinate)ps.pop();
      // check for empty stack to guard against robustness problems
      while (!ps.empty()
        && CGAlgorithms.computeOrientation((Coordinate)ps.peek(), p, c[i]) > 0) {
        p = (Coordinate)ps.pop();
      }
      p = (Coordinate)ps.push(p);
      p = (Coordinate)ps.push(c[i]);
    }
    p = (Coordinate)ps.push(c[0]);
    return ps;
  }

  /**
   *@return    whether the three coordinates are collinear and c2 lies between
   *      c1 and c3 inclusive
   */
  private boolean isBetween(final Coordinate c1, final Coordinate c2,
    final Coordinate c3) {
    if (CGAlgorithms.computeOrientation(c1, c2, c3) != 0) {
      return false;
    }
    if (c1.x != c3.x) {
      if (c1.x <= c2.x && c2.x <= c3.x) {
        return true;
      }
      if (c3.x <= c2.x && c2.x <= c1.x) {
        return true;
      }
    }
    if (c1.y != c3.y) {
      if (c1.y <= c2.y && c2.y <= c3.y) {
        return true;
      }
      if (c3.y <= c2.y && c2.y <= c1.y) {
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
  private Geometry lineOrPolygon(Coordinate[] coordinates) {

    coordinates = cleanRing(coordinates);
    if (coordinates.length == 3) {
      return geomFactory.createLineString(new Coordinate[] {
        coordinates[0], coordinates[1]
      });
      // return new LineString(new Coordinate[]{coordinates[0], coordinates[1]},
      // geometry.getPrecisionModel(), geometry.getSRID());
    }
    final LinearRing linearRing = geomFactory.createLinearRing(coordinates);
    return geomFactory.createPolygon(linearRing, null);
  }

  private Coordinate[] padArray3(final Coordinate[] pts) {
    final Coordinate[] pad = new Coordinate[3];
    for (int i = 0; i < pad.length; i++) {
      if (i < pts.length) {
        pad[i] = pts[i];
      } else {
        pad[i] = pts[0];
      }
    }
    return pad;
  }

  /*
   * // MD - no longer used, but keep for reference purposes private
   * Coordinate[] computeQuad(Coordinate[] inputPts) { BigQuad bigQuad =
   * bigQuad(inputPts); // Build a linear ring defining a big poly. ArrayList
   * bigPoly = new ArrayList(); bigPoly.add(bigQuad.westmost); if (!
   * bigPoly.contains(bigQuad.northmost)) { bigPoly.add(bigQuad.northmost); } if
   * (! bigPoly.contains(bigQuad.eastmost)) { bigPoly.add(bigQuad.eastmost); }
   * if (! bigPoly.contains(bigQuad.southmost)) {
   * bigPoly.add(bigQuad.southmost); } // points must all lie in a line if
   * (bigPoly.size() < 3) { return null; } // closing point
   * bigPoly.add(bigQuad.westmost); Coordinate[] bigPolyArray =
   * CoordinateArrays.toCoordinateArray(bigPoly); return bigPolyArray; } private
   * BigQuad bigQuad(Coordinate[] pts) { BigQuad bigQuad = new BigQuad();
   * bigQuad.northmost = pts[0]; bigQuad.southmost = pts[0]; bigQuad.westmost =
   * pts[0]; bigQuad.eastmost = pts[0]; for (int i = 1; i < pts.length; i++) {
   * if (pts[i].x < bigQuad.westmost.x) { bigQuad.westmost = pts[i]; } if
   * (pts[i].x > bigQuad.eastmost.x) { bigQuad.eastmost = pts[i]; } if (pts[i].y
   * < bigQuad.southmost.y) { bigQuad.southmost = pts[i]; } if (pts[i].y >
   * bigQuad.northmost.y) { bigQuad.northmost = pts[i]; } } return bigQuad; }
   * private static class BigQuad { public Coordinate northmost; public
   * Coordinate southmost; public Coordinate westmost; public Coordinate
   * eastmost; }
   */

  private Coordinate[] preSort(final Coordinate[] pts) {
    Coordinate t;

    // find the lowest point in the set. If two or more points have
    // the same minimum y coordinate choose the one with the minimu x.
    // This focal point is put in array location pts[0].
    for (int i = 1; i < pts.length; i++) {
      if ((pts[i].y < pts[0].y)
        || ((pts[i].y == pts[0].y) && (pts[i].x < pts[0].x))) {
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
  private Coordinate[] reduce(final Coordinate[] inputPts) {
    // Coordinate[] polyPts = computeQuad(inputPts);
    final Coordinate[] polyPts = computeOctRing(inputPts);
    // Coordinate[] polyPts = null;

    // unable to compute interior polygon for some reason
    if (polyPts == null) {
      return inputPts;
    }

    // LinearRing ring = geomFactory.createLinearRing(polyPts);
    // System.out.println(ring);

    // add points defining polygon
    final TreeSet reducedSet = new TreeSet();
    for (int i = 0; i < polyPts.length; i++) {
      reducedSet.add(polyPts[i]);
    }
    /**
     * Add all unique points not in the interior poly.
     * CGAlgorithms.isPointInRing is not defined for points actually on the ring,
     * but this doesn't matter since the points of the interior polygon
     * are forced to be in the reduced set.
     */
    for (int i = 0; i < inputPts.length; i++) {
      if (!CGAlgorithms.isPointInRing(inputPts[i], polyPts)) {
        reducedSet.add(inputPts[i]);
      }
    }
    final Coordinate[] reducedPts = CoordinateArrays.toCoordinateArray(reducedSet);

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
  protected Coordinate[] toCoordinateArray(final Stack stack) {
    final Coordinate[] coordinates = new Coordinate[stack.size()];
    for (int i = 0; i < stack.size(); i++) {
      final Coordinate coordinate = (Coordinate)stack.get(i);
      coordinates[i] = coordinate;
    }
    return coordinates;
  }
}
