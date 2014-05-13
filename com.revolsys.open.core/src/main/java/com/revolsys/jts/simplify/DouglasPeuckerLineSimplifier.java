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

package com.revolsys.jts.simplify;

import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.Point;

/**
 * Simplifies a linestring (sequence of points) using
 * the standard Douglas-Peucker algorithm.
 *
 * @version 1.7
 */
class DouglasPeuckerLineSimplifier {
  public static Point[] simplify(final Point[] pts,
    final double distanceTolerance) {
    final DouglasPeuckerLineSimplifier simp = new DouglasPeuckerLineSimplifier(
      pts);
    simp.setDistanceTolerance(distanceTolerance);
    return simp.simplify();
  }

  private final Point[] pts;

  private boolean[] usePt;

  private double distanceTolerance;

  public DouglasPeuckerLineSimplifier(final Point[] pts) {
    this.pts = pts;
  }

  /**
   * Sets the distance tolerance for the simplification.
   * All vertices in the simplified linestring will be within this
   * distance of the original linestring.
   *
   * @param distanceTolerance the approximation tolerance to use
   */
  public void setDistanceTolerance(final double distanceTolerance) {
    this.distanceTolerance = distanceTolerance;
  }

  public Point[] simplify() {
    usePt = new boolean[pts.length];
    for (int i = 0; i < pts.length; i++) {
      usePt[i] = true;
    }
    simplifySection(0, pts.length - 1);
    final CoordinateList coordList = new CoordinateList();
    for (int i = 0; i < pts.length; i++) {
      if (usePt[i]) {
        coordList.add(new Coordinate(pts[i]));
      }
    }
    return coordList.toCoordinateArray();
  }

  private void simplifySection(final int i, final int j) {
    if ((i + 1) == j) {
      return;
    }
    final Point p0 = pts[i];
    final Point p1 = pts[j];
    double maxDistance = -1.0;
    int maxIndex = i;
    for (int k = i + 1; k < j; k++) {
      final double distance = LineSegmentUtil.distance(p0, p1, pts[k]);
      if (distance > maxDistance) {
        maxDistance = distance;
        maxIndex = k;
      }
    }
    if (maxDistance <= distanceTolerance) {
      for (int k = i + 1; k < j; k++) {
        usePt[k] = false;
      }
    } else {
      simplifySection(i, maxIndex);
      simplifySection(maxIndex, j);
    }
  }

}
