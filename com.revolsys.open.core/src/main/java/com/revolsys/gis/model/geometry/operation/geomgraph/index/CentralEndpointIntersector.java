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
package com.revolsys.gis.model.geometry.operation.geomgraph.index;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;

/**
 * Computes an approximate intersection of two line segments
 * by taking the most central of the endpoints of the segments.
 * This is effective in cases where the segments are nearly parallel
 * and should intersect at an endpoint.
 * It is also a reasonable strategy for cases where the 
 * endpoint of one segment lies on or almost on the interior of another one.
 * Taking the most central endpoint ensures that the computed intersection
 * point lies in the envelope of the segments.
 * Also, by always returning one of the input points, this should result 
 * in reducing segment fragmentation.
 * Intended to be used as a last resort for 
 * computing ill-conditioned intersection situations which 
 * cause other methods to fail.
 *
 * @author Martin Davis
 * @version 1.8
 */
public class CentralEndpointIntersector {
  private static Coordinates average(final Coordinates[] pts) {
    final Coordinates avg = new DoubleCoordinates();
    final int n = pts.length;
    for (int i = 0; i < pts.length; i++) {
      avg.setX(avg.getX() + pts[i].getX());
      avg.setY(avg.getY() + pts[i].getY());
    }
    if (n > 0) {
      avg.setX(avg.getX() / n);
      avg.setY(avg.getY() / n);
    }
    return avg;
  }

  public static Coordinates getIntersection(final Coordinates p00,
    final Coordinates p01, final Coordinates p10, final Coordinates p11) {
    final CentralEndpointIntersector intor = new CentralEndpointIntersector(
      p00, p01, p10, p11);
    return intor.getIntersection();
  }

  private final Coordinates[] pts;

  private Coordinates intPt = null;

  public CentralEndpointIntersector(final Coordinates p00,
    final Coordinates p01, final Coordinates p10, final Coordinates p11) {
    pts = new Coordinates[] {
      p00, p01, p10, p11
    };
    compute();
  }

  private void compute() {
    final Coordinates centroid = average(pts);
    intPt = findNearestPoint(centroid, pts);
  }

  /**
   * Determines a point closest to the given point.
   * 
   * @param p the point to compare against
   * @param p1 a potential result point
   * @param p2 a potential result point
   * @param q1 a potential result point
   * @param q2 a potential result point
   * @return the point closest to the input point p
   */
  private Coordinates findNearestPoint(final Coordinates p,
    final Coordinates[] pts) {
    double minDist = Double.MAX_VALUE;
    Coordinates result = null;
    for (int i = 0; i < pts.length; i++) {
      final double dist = p.distance(pts[i]);
      if (dist < minDist) {
        minDist = dist;
        result = pts[i];
      }
    }
    return result;
  }

  public Coordinates getIntersection() {
    return intPt;
  }

}
