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
package com.revolsys.gis.model.geometry.algorithm.locate;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.Polygonal;
import com.revolsys.gis.model.geometry.algorithm.RayCrossingCounter;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.vividsolutions.jts.geom.Location;

/**
 * Computes the location of points relative to a {@link Polygonal}
 * {@link Geometry}, using a simple O(n) algorithm. This algorithm is suitable
 * for use in cases where only one or a few points will be tested against a
 * given area.
 * <p>
 * The algorithm used is only guaranteed to return correct results for points
 * which are <b>not</b> on the boundary of the Geometry.
 * 
 * @version 1.7
 */
public class SimplePointInAreaLocator implements PointOnGeometryLocator {

  private static boolean containsPoint(final Coordinates point,
    final Geometry geometry) {
    if (geometry instanceof Polygon) {
      return containsPointInPolygon(point, (Polygon)geometry);
    } else if (geometry instanceof GeometryCollection) {
      for (final Geometry geometry2 : geometry.getGeometries()) {
        if (geometry2 != geometry) {
          if (containsPoint(point, geometry2)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean containsPointInPolygon(final Coordinates p,
    final Polygon polygon) {
    if (polygon.isEmpty()) {
      return false;
    } else {
      boolean exterior = true;
      for (final LinearRing ring : polygon.getRings()) {
        final boolean pointInRing = isPointInRing(p, ring);
        // point is outside exterior ring or inside exterior rings
        if (pointInRing != exterior) {
          return false;
        }
        exterior = false;
      }
      return true;
    }
  }

  /**
   * Determines whether a point lies in a LinearRing, using the ring envelope to
   * short-circuit if possible.
   * 
   * @param point the point to test
   * @param ring a linear ring
   * @return true if the point lies inside the ring
   */
  private static boolean isPointInRing(final Coordinates point,
    final LinearRing ring) {
    final BoundingBox boundingBox = ring.getBoundingBox();
    if (boundingBox.intersects(point)) {
      return RayCrossingCounter.locatePointInRing(point, ring) != Location.EXTERIOR;
    } else {
      return false;
    }
  }

  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   * Currently this will never return a value of BOUNDARY.
   * 
   * @param point the point to test
   * @param geometry the areal geometry to test
   * @return the Location of the point in the geometry
   */
  public static int locate(final Coordinates point, final Geometry geometry) {
    if (geometry.isEmpty()) {
      return Location.EXTERIOR;
    } else if (containsPoint(point, geometry)) {
      return Location.INTERIOR;
    } else {
      return Location.EXTERIOR;
    }
  }

  private final Geometry geometry;

  public SimplePointInAreaLocator(final Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public int locate(final Coordinates point) {
    return SimplePointInAreaLocator.locate(point, geometry);
  }

}
