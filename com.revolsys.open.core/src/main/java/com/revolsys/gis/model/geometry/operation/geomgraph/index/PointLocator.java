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
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.GeometryFactory;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLineString;
import com.revolsys.gis.model.geometry.MultiLinearRing;
import com.revolsys.gis.model.geometry.MultiPolygon;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.algorithm.RayCrossingCounter;
import com.revolsys.gis.model.geometry.impl.BoundingBox;
import com.vividsolutions.jts.algorithm.BoundaryNodeRule;
import com.vividsolutions.jts.geom.Location;

/**
 * Computes the topological ({@link Location}) of a single point to a
 * {@link Geometry}. A {@link BoundaryNodeRule} may be specified to control the
 * evaluation of whether the point lies on the boundary or not The default rule
 * is to use the the <i>SFS Boundary Determination Rule</i>
 * <p>
 * Notes:
 * <ul>
 * <li>{@link LinearRing}s do not enclose any area - points inside the ring are
 * still in the EXTERIOR of the ring.
 * </ul>
 * Instances of this class are not reentrant.
 * 
 * @version 1.7
 */
public class PointLocator {
  // default is to use OGC SFS rule
  private BoundaryNodeRule boundaryRule =
  // BoundaryNodeRule.ENDPOINT_BOUNDARY_RULE;
  BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE;

  private boolean isIn; // true if the point lies in or on any Geometry element

  private int numBoundaries; // the number of sub-elements whose boundaries the
                             // point lies in

  private GeometryFactory geometryFactory;

  public PointLocator() {
  }

  public PointLocator(BoundaryNodeRule boundaryRule) {
    if (boundaryRule == null)
      throw new IllegalArgumentException("Rule must be non-null");
    this.boundaryRule = boundaryRule;
  }

  public PointLocator(GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  /**
   * Convenience method to test a point for intersection with a Geometry
   * 
   * @param p the coordinate to test
   * @param geom the Geometry to test
   * @return <code>true</code> if the point is in the interior or boundary of
   *         the Geometry
   */
  public boolean intersects(Coordinates p, Geometry geom) {
    return locate(p, geom) != Location.EXTERIOR;
  }

  /**
   * Computes the topological relationship ({@link Location}) of a single point
   * to a Geometry. It handles both single-element and multi-element Geometries.
   * The algorithm for multi-part Geometries takes into account the SFS Boundary
   * Determination Rule.
   * 
   * @return the {@link Location} of the point relative to the input Geometry
   */
  public int locate(Coordinates p, Geometry geom) {
    if (geom.isEmpty())
      return Location.EXTERIOR;

    if (geom instanceof LineString) {
      return locate(p, (LineString)geom);
    } else if (geom instanceof Polygon) {
      return locate(p, (Polygon)geom);
    }

    isIn = false;
    numBoundaries = 0;
    computeLocation(p, geom);
    if (boundaryRule.isInBoundary(numBoundaries))
      return Location.BOUNDARY;
    if (numBoundaries > 0 || isIn)
      return Location.INTERIOR;

    return Location.EXTERIOR;
  }

  private void computeLocation(Coordinates p, Geometry geom) {
    if (geom instanceof Point) {
      updateLocationInfo(locate(p, (Point)geom));
    }
    if (geom instanceof LineString) {
      updateLocationInfo(locate(p, (LineString)geom));
    } else if (geom instanceof Polygon) {
      updateLocationInfo(locate(p, (Polygon)geom));
    } else if (geom instanceof MultiLineString) {
      MultiLineString ml = (MultiLineString)geom;
      for (int i = 0; i < ml.getGeometryCount(); i++) {
        LineString l = (LineString)ml.getGeometry(i);
        updateLocationInfo(locate(p, l));
      }
    } else if (geom instanceof MultiPolygon) {
      MultiPolygon mpoly = (MultiPolygon)geom;
      for (int i = 0; i < mpoly.getGeometryCount(); i++) {
        Polygon poly = (Polygon)mpoly.getGeometry(i);
        updateLocationInfo(locate(p, poly));
      }
    } else if (geom instanceof GeometryCollection) {
      for (Geometry g2 : geom.getGeometries()) {
        if (g2 != geom)
          computeLocation(p, g2);
      }
    }
  }

  private void updateLocationInfo(int loc) {
    if (loc == Location.INTERIOR)
      isIn = true;
    if (loc == Location.BOUNDARY)
      numBoundaries++;
  }

  private int locate(Coordinates p, Point point) {
    if (point.equals2d(p)) {
      return Location.INTERIOR;
    } else {
      return Location.EXTERIOR;
    }
  }

  private int locate(Coordinates p, LineString l) {
    // bounding-box check
    if (!l.getBoundingBox().intersects(p))
      return Location.EXTERIOR;

    if (!l.isClosed()) {
      if (p.equals(l.get(0)) || p.equals(l.get(l.size() - 1))) {
        return Location.BOUNDARY;
      }
    }
    if (CoordinatesListUtil.isPointOnLine(geometryFactory, l,p)) {
      return Location.INTERIOR;
    } else {
      return Location.EXTERIOR;
    }
  }

  private int locateInPolygonRing(Coordinates p, LinearRing ring) {
    BoundingBox boundingBox = ring.getBoundingBox();
    if (boundingBox.intersects(p)) {
      return RayCrossingCounter.locatePointInRing(p, ring);
    } else {
      return Location.EXTERIOR;
    }
  }

  private int locate(Coordinates p, Polygon poly) {
    if (poly.isEmpty()) {
      return Location.EXTERIOR;
    } else {
      MultiLinearRing rings = poly.getRings();
      LinearRing shell = poly.getExteriorRing();

      int shellLoc = locateInPolygonRing(p, shell);
      if (shellLoc == Location.EXTERIOR)
        return Location.EXTERIOR;
      if (shellLoc == Location.BOUNDARY)
        return Location.BOUNDARY;
      // now test if the point lies in or on the holes
      for (int i = 1; i < rings.getGeometryCount(); i++) {
        LinearRing hole = rings.getGeometry(i);
        int holeLoc = locateInPolygonRing(p, hole);
        if (holeLoc == Location.INTERIOR)
          return Location.EXTERIOR;
        if (holeLoc == Location.BOUNDARY)
          return Location.BOUNDARY;
      }
      return Location.INTERIOR;
    }
  }

}
