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

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

/**
 * Computes the topological ({@link Location})
 * of a single point to a {@link Geometry}.
 * A {@link BoundaryNodeRule} may be specified
 * to control the evaluation of whether the point lies on the boundary or not
 * The default rule is to use the the <i>SFS Boundary Determination Rule</i>
 * <p>
 * Notes:
 * <ul>
 * <li>{@link LinearRing}s do not enclose any area - points inside the ring are still in the EXTERIOR of the ring.
 * </ul>
 * Instances of this class are not reentrant.
 *
 * @version 1.7
 */
public class PointLocator {
  private final BoundaryNodeRule boundaryRule = BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE;

  private boolean isIn; // true if the point lies in or on any Geometry element

  public PointLocator() {
  }

  private int computeLocation(final Point point, final Geometry geometry) {
    int numBoundaries = 0;
    if (geometry instanceof Point) {
      return updateLocationInfo(locate(point, (Point)geometry));
    } else if (geometry instanceof LineString) {
      return updateLocationInfo(locate(point, (LineString)geometry));
    } else if (geometry instanceof Polygon) {
      return updateLocationInfo(locate(point, (Polygon)geometry));
    } else {
      for (final Geometry part : geometry.geometries()) {
        if (part != geometry) {
          numBoundaries += computeLocation(point, part);
        }
      }
    }
    return numBoundaries;
  }

  /**
   * Convenience method to test a point for intersection with
   * a Geometry
   * @param point the coordinate to test
   * @param geometry the Geometry to test
   * @return <code>true</code> if the point is in the interior or boundary of the Geometry
   */
  public boolean intersects(final Point point, final Geometry geometry) {
    return locate(point, geometry) != Location.EXTERIOR;
  }

  /**
   * Computes the topological relationship ({@link Location}) of a single point
   * to a Geometry.
   * It handles both single-element
   * and multi-element Geometries.
   * The algorithm for multi-part Geometries
   * takes into account the SFS Boundary Determination Rule.
   *
   * @return the {@link Location} of the point relative to the input Geometry
   */
  public Location locate(final Point point, final Geometry geometry) {
    if (geometry.isEmpty()) {
      return Location.EXTERIOR;
    } else if (geometry instanceof LineString) {
      return locate(point, (LineString)geometry);
    } else if (geometry instanceof Polygon) {
      return locate(point, (Polygon)geometry);
    }

    this.isIn = false;
    final int numBoundaries = computeLocation(point, geometry);
    if (this.boundaryRule.isInBoundary(numBoundaries)) {
      return Location.BOUNDARY;
    }
    if (numBoundaries > 0 || this.isIn) {
      return Location.INTERIOR;
    }

    return Location.EXTERIOR;
  }

  private Location locate(final Point point, final LineString line) {
    // bounding-box check
    if (point.intersects(line.getBoundingBox())) {
      if (!line.isClosed()) {
        if (point.equals(line.getVertex(0)) || point.equals(line.getVertex(-1))) {
          return Location.BOUNDARY;
        }
      }
      if (CGAlgorithms.isOnLine(point, line)) {
        return Location.INTERIOR;
      }
    }
    return Location.EXTERIOR;
  }

  private Location locate(final Point p, final Point pt) {
    // no point in doing envelope test, since equality test is just as fast

    final Point ptCoord = pt.getPoint();
    if (ptCoord.equals(2, p)) {
      return Location.INTERIOR;
    }
    return Location.EXTERIOR;
  }

  private Location locate(final Point p, final Polygon poly) {
    if (poly.isEmpty()) {
      return Location.EXTERIOR;
    }

    final LinearRing shell = poly.getExteriorRing();

    final Location shellLoc = locateInPolygonRing(p, shell);
    if (shellLoc == Location.EXTERIOR) {
      return Location.EXTERIOR;
    }
    if (shellLoc == Location.BOUNDARY) {
      return Location.BOUNDARY;
    }
    // now test if the point lies in or on the holes
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      final LinearRing hole = poly.getInteriorRing(i);
      final Location holeLoc = locateInPolygonRing(p, hole);
      if (holeLoc == Location.INTERIOR) {
        return Location.EXTERIOR;
      }
      if (holeLoc == Location.BOUNDARY) {
        return Location.BOUNDARY;
      }
    }
    return Location.INTERIOR;
  }

  private Location locateInPolygonRing(final Point p, final LinearRing ring) {
    // bounding-box check
    if (!p.intersects(ring.getBoundingBox())) {
      return Location.EXTERIOR;
    }

    return RayCrossingCounter.locatePointInRing(p, ring);
  }

  private int updateLocationInfo(final Location loc) {

    if (loc == Location.INTERIOR) {
      this.isIn = true;
    } else if (loc == Location.BOUNDARY) {
      return 1;
    }
    return 0;
  }

}
