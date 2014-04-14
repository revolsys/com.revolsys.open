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

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

/**
 * Computes the minimum diameter of a {@link Geometry}.
 * The minimum diameter is defined to be the
 * width of the smallest band that
 * contains the geometry,
 * where a band is a strip of the plane defined
 * by two parallel lines.
 * This can be thought of as the smallest hole that the geometry can be
 * moved through, with a single rotation.
 * <p>
 * The first step in the algorithm is computing the convex hull of the Geometry.
 * If the input Geometry is known to be convex, a hint can be supplied to
 * avoid this computation.
 * <p>
 * This class can also be used to compute a line segment representing 
 * the minimum diameter, the supporting line segment of the minimum diameter,
 * and a minimum rectangle enclosing the input geometry.
 * This rectangle will
 * have width equal to the minimum diameter, and have one side
 * parallel to the supporting segment.
 *
 * @see ConvexHull
 *
 * @version 1.7
 */
public class MinimumDiameter {
  private static double computeC(final double a, final double b,
    final Coordinates p) {
    return a * p.getY() - b * p.getX();
  }

  private static LineSegment computeSegmentForLine(final double a,
    final double b, final double c) {
    Coordinates p0;
    Coordinates p1;
    /*
     * Line eqn is ax + by = c Slope is a/b. If slope is steep, use y values as
     * the inputs
     */
    if (Math.abs(b) > Math.abs(a)) {
      p0 = new Coordinate(0.0, c / b, Coordinates.NULL_ORDINATE);
      p1 = new Coordinate(1.0, c / b - a / b, Coordinates.NULL_ORDINATE);
    } else {
      p0 = new Coordinate(c / a, 0.0, Coordinates.NULL_ORDINATE);
      p1 = new Coordinate(c / a - b / a, 1.0, Coordinates.NULL_ORDINATE);
    }
    return new LineSegment(p0, p1);
  }

  private static int nextIndex(final Coordinates[] pts, int index) {
    index++;
    if (index >= pts.length) {
      index = 0;
    }
    return index;
  }

  private final Geometry inputGeom;

  private final boolean isConvex;

  private Coordinates[] convexHullPts = null;

  private LineSegment minBaseSeg = new LineSegment();

  private Coordinates minWidthPt = null;

  private int minPtIndex;

  private double minWidth = 0.0;

  /**
   * Compute a minimum diameter for a given {@link Geometry}.
   *
   * @param geom a Geometry
   */
  public MinimumDiameter(final Geometry inputGeom) {
    this(inputGeom, false);
  }

  /**
   * Compute a minimum diameter for a giver {@link Geometry},
   * with a hint if
   * the Geometry is convex
   * (e.g. a convex Polygon or LinearRing,
   * or a two-point LineString, or a Point).
   *
   * @param geom a Geometry which is convex
   * @param isConvex <code>true</code> if the input geometry is convex
   */
  public MinimumDiameter(final Geometry inputGeom, final boolean isConvex) {
    this.inputGeom = inputGeom;
    this.isConvex = isConvex;
  }

  /**
   * Compute the width information for a ring of {@link Coordinates}s.
   * Leaves the width information in the instance variables.
   *
   * @param pts
   */
  private void computeConvexRingMinDiameter(final Coordinates[] pts) {
    // for each segment in the ring
    minWidth = Double.MAX_VALUE;
    int currMaxIndex = 1;

    final LineSegment seg = new LineSegment();
    // compute the max distance for all segments in the ring, and pick the
    // minimum
    for (int i = 0; i < pts.length - 1; i++) {
      seg.p0 = pts[i];
      seg.p1 = pts[i + 1];
      currMaxIndex = findMaxPerpDistance(pts, seg, currMaxIndex);
    }
  }

  private void computeMinimumDiameter() {
    // check if computation is cached
    if (minWidthPt != null) {
      return;
    }

    if (isConvex) {
      computeWidthConvex(inputGeom);
    } else {
      final Geometry convexGeom = (new ConvexHull(inputGeom)).getConvexHull();
      computeWidthConvex(convexGeom);
    }
  }

  private void computeWidthConvex(final Geometry convexGeom) {
    // System.out.println("Input = " + geom);
    if (convexGeom instanceof Polygon) {
      convexHullPts = ((Polygon)convexGeom).getExteriorRing()
        .getCoordinateArray();
    } else {
      convexHullPts = convexGeom.getCoordinateArray();
    }

    // special cases for lines or points or degenerate rings
    if (convexHullPts.length == 0) {
      minWidth = 0.0;
      minWidthPt = null;
      minBaseSeg = null;
    } else if (convexHullPts.length == 1) {
      minWidth = 0.0;
      minWidthPt = convexHullPts[0];
      minBaseSeg.p0 = convexHullPts[0];
      minBaseSeg.p1 = convexHullPts[0];
    } else if (convexHullPts.length == 2 || convexHullPts.length == 3) {
      minWidth = 0.0;
      minWidthPt = convexHullPts[0];
      minBaseSeg.p0 = convexHullPts[0];
      minBaseSeg.p1 = convexHullPts[1];
    } else {
      computeConvexRingMinDiameter(convexHullPts);
    }
  }

  private int findMaxPerpDistance(final Coordinates[] pts,
    final LineSegment seg, final int startIndex) {
    double maxPerpDistance = seg.distancePerpendicular(pts[startIndex]);
    double nextPerpDistance = maxPerpDistance;
    int maxIndex = startIndex;
    int nextIndex = maxIndex;
    while (nextPerpDistance >= maxPerpDistance) {
      maxPerpDistance = nextPerpDistance;
      maxIndex = nextIndex;

      nextIndex = nextIndex(pts, maxIndex);
      nextPerpDistance = seg.distancePerpendicular(pts[nextIndex]);
    }
    // found maximum width for this segment - update global min dist if
    // appropriate
    if (maxPerpDistance < minWidth) {
      minPtIndex = maxIndex;
      minWidth = maxPerpDistance;
      minWidthPt = pts[minPtIndex];
      minBaseSeg = new LineSegment(seg);
      // System.out.println(minBaseSeg);
      // System.out.println(minWidth);
    }
    return maxIndex;
  }

  /**
   * Gets a {@link LineString} which is a minimum diameter
   *
   * @return a {@link LineString} which is a minimum diameter
   */
  public LineString getDiameter() {
    computeMinimumDiameter();

    // return empty linestring if no minimum width calculated
    if (minWidthPt == null) {
      return inputGeom.getGeometryFactory().lineString(
        (Coordinates[])null);
    }

    final Coordinates basePt = minBaseSeg.project(minWidthPt);
    return inputGeom.getGeometryFactory().lineString(new Coordinates[] {
      basePt, minWidthPt
    });
  }

  /**
   * Gets the length of the minimum diameter of the input Geometry
   *
   * @return the length of the minimum diameter
   */
  public double getLength() {
    computeMinimumDiameter();
    return minWidth;
  }

  /**
   * Gets the minimum rectangular {@link Polygon} which encloses the input geometry.
   * The rectangle has width equal to the minimum diameter, 
   * and a longer length.
   * If the convex hull of the input is degenerate (a line or point)
   * a {@link LineString} or {@link Point} is returned.
   * <p>
   * The minimum rectangle can be used as an extremely generalized representation
   * for the given geometry.
   * 
   * @return the minimum rectangle enclosing the input (or a line or point if degenerate)
   */
  public Geometry getMinimumRectangle() {
    computeMinimumDiameter();

    // check if minimum rectangle is degenerate (a point or line segment)
    if (minWidth == 0.0) {
      if (minBaseSeg.p0.equals2d(minBaseSeg.p1)) {
        return inputGeom.getGeometryFactory().point(minBaseSeg.p0);
      }
      return minBaseSeg.toGeometry(inputGeom.getGeometryFactory());
    }

    // deltas for the base segment of the minimum diameter
    final double dx = minBaseSeg.p1.getX() - minBaseSeg.p0.getX();
    final double dy = minBaseSeg.p1.getY() - minBaseSeg.p0.getY();

    /*
     * double c0 = computeC(dx, dy, minBaseSeg.p0); double c1 = computeC(dx, dy,
     * minBaseSeg.p1);
     */

    double minPara = Double.MAX_VALUE;
    double maxPara = -Double.MAX_VALUE;
    double minPerp = Double.MAX_VALUE;
    double maxPerp = -Double.MAX_VALUE;

    // compute maxima and minima of lines parallel and perpendicular to base
    // segment
    for (int i = 0; i < convexHullPts.length; i++) {

      final double paraC = computeC(dx, dy, convexHullPts[i]);
      if (paraC > maxPara) {
        maxPara = paraC;
      }
      if (paraC < minPara) {
        minPara = paraC;
      }

      final double perpC = computeC(-dy, dx, convexHullPts[i]);
      if (perpC > maxPerp) {
        maxPerp = perpC;
      }
      if (perpC < minPerp) {
        minPerp = perpC;
      }
    }

    // compute lines along edges of minimum rectangle
    final LineSegment maxPerpLine = computeSegmentForLine(-dx, -dy, maxPerp);
    final LineSegment minPerpLine = computeSegmentForLine(-dx, -dy, minPerp);
    final LineSegment maxParaLine = computeSegmentForLine(-dy, dx, maxPara);
    final LineSegment minParaLine = computeSegmentForLine(-dy, dx, minPara);

    // compute vertices of rectangle (where the para/perp max & min lines
    // intersect)
    final Coordinates p0 = maxParaLine.lineIntersection(maxPerpLine);
    final Coordinates p1 = minParaLine.lineIntersection(maxPerpLine);
    final Coordinates p2 = minParaLine.lineIntersection(minPerpLine);
    final Coordinates p3 = maxParaLine.lineIntersection(minPerpLine);

    final LinearRing shell = inputGeom.getGeometryFactory().createLinearRing(
      new Coordinates[] {
        p0, p1, p2, p3, p0
      });
    return inputGeom.getGeometryFactory().createPolygon(shell, null);

  }

  /**
   * Gets the segment forming the base of the minimum diameter
   *
   * @return the segment forming the base of the minimum diameter
   */
  public LineString getSupportingSegment() {
    computeMinimumDiameter();
    return inputGeom.getGeometryFactory().lineString(new Coordinates[] {
      minBaseSeg.p0, minBaseSeg.p1
    });
  }

  /**
   * Gets the {@link Coordinates} forming one end of the minimum diameter
   *
   * @return a coordinate forming one end of the minimum diameter
   */
  public Coordinates getWidthCoordinate() {
    computeMinimumDiameter();
    return minWidthPt;
  }
}
