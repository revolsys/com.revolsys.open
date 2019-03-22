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
package com.revolsys.geometry.algorithm.locate;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.algorithm.RayCrossingCounter;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Location;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;

/**
 * Computes the location of points
 * relative to a {@link Polygonal} {@link Geometry},
 * using a simple O(n) algorithm.
 * This algorithm is suitable for use in cases where
 * only one or a few points will be tested against a given area.
 * <p>
 * The algorithm used is only guaranteed to return correct results
 * for points which are <b>not</b> on the boundary of the Geometry.
 *
 * @version 1.7
 */
public class SimplePointInAreaLocator implements PointOnGeometryLocator {

  private static boolean containsPoint(final RayCrossingCounter rayCrossingCounter,
    final Geometry geom, final double x, final double y) {
    if (geom instanceof Polygon) {
      return containsPointInPolygon(rayCrossingCounter, (Polygon)geom, x, y);
    } else if (geom.isGeometryCollection()) {
      for (int i = 0; i < geom.getGeometryCount(); i++) {
        final Geometry g2 = geom.getGeometry(i);
        if (g2 != geom) {
          if (containsPoint(rayCrossingCounter, g2, x, y)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean containsPointInPolygon(final Point p, final Polygon poly) {
    if (poly.isEmpty()) {
      return false;
    }
    final LinearRing shell = poly.getShell();
    if (!isPointInRing(p, shell)) {
      return false;
    }
    // now test if the point lies in or on the holes
    for (int i = 0; i < poly.getHoleCount(); i++) {
      final LinearRing hole = poly.getHole(i);
      if (isPointInRing(p, hole)) {
        return false;
      }
    }
    return true;
  }

  private static boolean containsPointInPolygon(final RayCrossingCounter rayCrossingCounter,
    final Polygon poly, final double x, final double y) {
    if (poly.isEmpty()) {
      return false;
    } else {
      final LinearRing shell = poly.getShell();
      if (!isPointInRing(rayCrossingCounter, shell, x, y)) {
        return false;
      }
      // now test if the point lies in or on the holes
      for (int i = 0; i < poly.getHoleCount(); i++) {
        final LinearRing hole = poly.getHole(i);
        if (isPointInRing(rayCrossingCounter, hole, x, y)) {
          return false;
        }
      }
      return true;
    }
  }

  /**
   * Determines whether a point lies in a LinearRing,
   * using the ring envelope to short-circuit if possible.
   *
   * @param p the point to test
   * @param ring a linear ring
   * @return true if the point lies inside the ring
   */
  private static boolean isPointInRing(final Point p, final LinearRing ring) {
    // short-circuit if point is not in ring envelope
    if (!p.intersects(ring.getBoundingBox())) {
      return false;
    }
    return CGAlgorithms.isPointInRing(p, ring);
  }

  /**
   * Determines whether a point lies in a LinearRing,
   * using the ring envelope to short-circuit if possible.
   * @param counter
   *
   * @param p the point to test
   * @param ring a linear ring
   * @return true if the point lies inside the ring
   */
  private static boolean isPointInRing(final RayCrossingCounter counter, final LinearRing ring,
    final double x, final double y) {
    final BoundingBox boundingBox = ring.getBoundingBox();
    if (boundingBox.intersects(x, y)) {
      counter.clear();
      double x0 = ring.getX(0);
      double y0 = ring.getY(0);
      for (int i = 1; i < ring.getVertexCount(); i++) {
        final double x1 = ring.getX(i);
        final double y1 = ring.getY(i);
        counter.countSegment(x1, y1, x0, y0);
        if (counter.isOnSegment()) {
          return counter.getLocation() != Location.EXTERIOR;
        }
        x0 = x1;
        y0 = y1;
      }
      return counter.getLocation() != Location.EXTERIOR;
    } else {
      return false;
    }
  }

  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   * Currently this will never return a value of BOUNDARY.
   * @param geometry the areal geometry to test
   * @param point the point to test
   *
   * @return the Location of the point in the geometry
   */
  public static Location locate(final Geometry geometry, final Point point) {
    if (geometry.isEmpty()) {
      return Location.EXTERIOR;
    } else {
      final double x = point.getX();
      final double y = point.getY();
      final RayCrossingCounter rayCrossingCounter = new RayCrossingCounter(x, y);
      if (containsPoint(rayCrossingCounter, geometry, x, y)) {
        return Location.INTERIOR;
      }
      return Location.EXTERIOR;
    }
  }

  private final Geometry geom;

  public SimplePointInAreaLocator(final Geometry geom) {
    this.geom = geom;
  }

  @Override
  public Location locate(final Point p) {
    return SimplePointInAreaLocator.locate(this.geom, p);
  }

}
