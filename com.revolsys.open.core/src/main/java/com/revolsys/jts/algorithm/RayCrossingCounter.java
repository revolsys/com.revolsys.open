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

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Polygonal;

/**
 * Counts the number of segments crossed by a horizontal ray extending to the right
 * from a given point, in an incremental fashion.
 * This can be used to determine whether a point lies in a {@link Polygonal} geometry.
 * The class determines the situation where the point lies exactly on a segment.
 * When being used for Point-In-Polygon determination, this case allows short-circuiting
 * the evaluation.
 * <p>
 * This class handles polygonal geometries with any number of shells and holes.
 * The orientation of the shell and hole rings is unimportant.
 * In order to compute a correct location for a given polygonal geometry, 
 * it is essential that <b>all</b> segments are counted which
 * <ul>
 * <li>touch the ray 
 * <li>lie in in any ring which may contain the point
 * </ul>
 * The only exception is when the point-on-segment situation is detected, in which
 * case no further processing is required.
 * The implication of the above rule is that segments 
 * which can be a priori determined to <i>not</i> touch the ray
 * (i.e. by a test of their bounding box or Y-extent) 
 * do not need to be counted.  This allows for optimization by indexing.
 * 
 * @author Martin Davis
 *
 */
public class RayCrossingCounter {
  /**
   * Determines the {@link Location} of a point in a ring.
   * This method is an exemplar of how to use this class.
   * 
   * @param p the point to test
   * @param ring an array of Coordinates forming a ring 
   * @return the location of the point in the ring
   */
  public static Location locatePointInRing(final Coordinates p,
    final Coordinates[] ring) {
    final RayCrossingCounter counter = new RayCrossingCounter(p);

    for (int i = 1; i < ring.length; i++) {
      final Coordinates p1 = ring[i];
      final Coordinates p2 = ring[i - 1];
      counter.countSegment(p1, p2);
      if (counter.isOnSegment()) {
        return counter.getLocation();
      }
    }
    return counter.getLocation();
  }

  /**
  * Determines the {@link Location} of a point in a ring. 
  * 
  * @param p
  *            the point to test
  * @param ring
  *            a coordinate sequence forming a ring
  * @return the location of the point in the ring
  */
  public static Location locatePointInRing(final Coordinates coordinates,
    final CoordinatesList ring) {
    final RayCrossingCounter counter = new RayCrossingCounter(coordinates);

    double x0 = ring.getX(0);
    double y0 = ring.getY(0);
    for (int i = 1; i < ring.size(); i++) {
      final double x1 = ring.getX(i);
      final double y1 = ring.getY(i);
      counter.countSegment(x1, y1, x0, y0);
      if (counter.isOnSegment()) {
        return counter.getLocation();
      }
      x0 = x1;
      y0 = y1;
    }
    return counter.getLocation();
  }

  /**
  * Determines the {@link Location} of a point in a ring. 
  * 
  * @param p
  *            the point to test
  * @param ring
  *            a coordinate sequence forming a ring
  * @return the location of the point in the ring
  */
  public static Location locatePointInRing(final Coordinates coordinates,
    final LineString ring) {
    final RayCrossingCounter counter = new RayCrossingCounter(coordinates);

    double x0 = ring.getX(0);
    double y0 = ring.getY(0);
    for (int i = 1; i < ring.getVertexCount(); i++) {
      final double x1 = ring.getX(i);
      final double y1 = ring.getY(i);
      counter.countSegment(x1, y1, x0, y0);
      if (counter.isOnSegment()) {
        return counter.getLocation();
      }
      x0 = x1;
      y0 = y1;
    }
    return counter.getLocation();
  }

  private final double x;

  private final double y;

  private int crossingCount = 0;

  // true if the test point lies on an input segment
  private boolean pointOnSegment = false;

  public RayCrossingCounter(final Coordinates point) {
    this(point.getX(), point.getY());
  }

  public RayCrossingCounter(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  /**
    * For each segment, check if it crosses a horizontal ray running from the
     * test point in the positive x direction.
    * 
   * @param p1 an endpoint of the segment
   * @param p2 another endpoint of the segment
   */
  public void countSegment(final Coordinates p1, final Coordinates p2) {

    final double x1 = p1.getX();
    final double y1 = p1.getY();
    final double x2 = p2.getX();
    final double y2 = p2.getY();

    countSegment(x1, y1, x2, y2);
  }

  public void countSegment(final double x1, final double y1, final double x2,
    final double y2) {
    if (x1 < x && x2 < x) {
      // check if the segment is strictly to the left of the test point
    } else if (x == x2 && y == y2) {
      // check if the point is equal to the current ring vertex
      pointOnSegment = true;
    } else if (y1 == y && y2 == y) {
      /**
       * For horizontal segments, check if the point is on the segment. Otherwise,
       * horizontal segments are not counted.
       */
      double minX = x1;
      double maxX = x2;
      if (minX > maxX) {
        minX = x2;
        maxX = x1;
      }
      if (x >= minX && x <= maxX) {
        pointOnSegment = true;
      }
    } else if ((y1 > y && y2 <= y) || (y2 > y && y1 <= y)) {
      /**
       * Evaluate all non-horizontal segments which cross a horizontal ray to the
       * right of the test pt. To avoid double-counting shared vertices, we use
       * the convention that
       * <ul>
       * <li>an upward edge includes its starting endpoint, and excludes its final
       * endpoint
       * <li>a downward edge excludes its starting endpoint, and includes its
       * final endpoint
       * </ul>
       */
      // translate the segment so that the test point lies on the origin
      final double deltaX1 = x1 - x;
      final double deltaY1 = y1 - y;
      final double deltaX2 = x2 - x;
      final double deltaY2 = y2 - y;

      /**
       * The translated segment straddles the x-axis. Compute the sign of the
       * ordinate of intersection with the x-axis. (y2 != y1, so denominator
       * will never be 0.0)
       */
      // double xIntSign = RobustDeterminant.signOfDet2x2(x1, y1, x2, y2) / (y2
      // - y1);
      // MD - faster & more robust computation?
      double xIntSign = RobustDeterminant.signOfDet2x2(deltaX1, deltaY1,
        deltaX2, deltaY2);
      if (xIntSign == 0.0) {
        pointOnSegment = true;
      } else {
        if (deltaY2 < deltaY1) {
          xIntSign = -xIntSign;
          // xsave = xInt;
        }

        // System.out.println("xIntSign(" + x1 + ", " + y1 + ", " + x2 + ", " +
        // y2
        // + " = " + xIntSign);
        // The segment crosses the ray if the sign is strictly positive.
        if (xIntSign > 0.0) {
          crossingCount++;
        }
      }
    }
  }

  /**
   * Gets the {@link Location} of the point relative to 
   * the ring, polygon
   * or multipolygon from which the processed segments were provided.
   * <p>
   * This method only determines the correct location 
   * if <b>all</b> relevant segments must have been processed. 
   * 
   * @return the Location of the point
   */
  public Location getLocation() {
    if (pointOnSegment) {
      return Location.BOUNDARY;
    }

    // The point is in the interior of the ring if the number of X-crossings is
    // odd.
    if ((crossingCount % 2) == 1) {
      return Location.INTERIOR;
    }
    return Location.EXTERIOR;
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  /**
   * Reports whether the point lies exactly on one of the supplied segments.
   * This method may be called at any time as segments are processed.
   * If the result of this method is <tt>true</tt>, 
   * no further segments need be supplied, since the result
   * will never change again.
   * 
   * @return true if the point lies exactly on a segment
   */
  public boolean isOnSegment() {
    return pointOnSegment;
  }

  /**
   * Tests whether the point lies in or on 
   * the ring, polygon
   * or multipolygon from which the processed segments were provided.
   * <p>
   * This method only determines the correct location 
   * if <b>all</b> relevant segments must have been processed. 
   * 
   * @return true if the point lies in or on the supplied polygon
   */
  public boolean isPointInPolygon() {
    return getLocation() != Location.EXTERIOR;
  }

  public void setPointOnSegment(final boolean pointOnSegment) {
    this.pointOnSegment = pointOnSegment;
  }
}
