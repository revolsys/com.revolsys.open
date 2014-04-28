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
package com.revolsys.jts.algorithm.distance;

import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineSegmentImpl;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Polygon;

/**
 * Computes the Euclidean distance (L2 metric) from a {@link Coordinates} to a {@link Geometry}.
 * Also computes two points on the geometry which are separated by the distance found.
 */
public class DistanceToPoint {

  public static void computeDistance(final Geometry geom, final Coordinates pt,
    final PointPairDistance ptDist) {
    if (geom instanceof LineString) {
      final LineString line = (LineString)geom;
      computeDistance(line, pt, ptDist);
    } else if (geom instanceof Polygon) {
      final Polygon polygon = (Polygon)geom;
      computeDistance(polygon, pt, ptDist);
    } else if (geom instanceof GeometryCollection) {
      final GeometryCollection gc = (GeometryCollection)geom;
      for (int i = 0; i < gc.getGeometryCount(); i++) {
        final Geometry g = gc.getGeometry(i);
        computeDistance(g, pt, ptDist);
      }
    } else { // assume geom is Point
      ptDist.setMinimum(geom.getCoordinate(), pt);
    }
  }

  public static void computeDistance(final LineSegment segment,
    final Coordinates pt, final PointPairDistance ptDist) {
    final Coordinates closestPt = segment.closestPoint(pt);
    ptDist.setMinimum(closestPt, pt);
  }

  public static void computeDistance(final LineString line,
    final Coordinates pt, final PointPairDistance ptDist) {
    final LineSegment tempSegment = new LineSegmentImpl();
    final Coordinates[] coords = line.getCoordinateArray();
    for (int i = 0; i < coords.length - 1; i++) {
      tempSegment.setCoordinates(coords[i], coords[i + 1]);
      // this is somewhat inefficient - could do better
      final Coordinates closestPt = tempSegment.closestPoint(pt);
      ptDist.setMinimum(closestPt, pt);
    }
  }

  public static void computeDistance(final Polygon poly, final Coordinates pt,
    final PointPairDistance ptDist) {
    computeDistance(poly.getExteriorRing(), pt, ptDist);
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      computeDistance(poly.getInteriorRing(i), pt, ptDist);
    }
  }

  public DistanceToPoint() {
  }
}
