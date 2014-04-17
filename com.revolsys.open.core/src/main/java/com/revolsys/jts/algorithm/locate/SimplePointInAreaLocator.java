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
package com.revolsys.jts.algorithm.locate;

import java.util.Iterator;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryCollectionIterator;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.Polygonal;

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
public class SimplePointInAreaLocator
	implements PointOnGeometryLocator
{

  /**
   * Determines the {@link Location} of a point in an areal {@link Geometry}.
   * Currently this will never return a value of BOUNDARY.  
   * 
   * @param p the point to test
   * @param geom the areal geometry to test
   * @return the Location of the point in the geometry  
   */
  public static int locate(Coordinates p, Geometry geom)
  {
    if (geom.isEmpty()) return Location.EXTERIOR;

    if (containsPoint(p, geom))
      return Location.INTERIOR;
    return Location.EXTERIOR;
  }

  private static boolean containsPoint(Coordinates p, Geometry geom)
  {
    if (geom instanceof Polygon) {
      return containsPointInPolygon(p, (Polygon) geom);
    }
    else if (geom instanceof GeometryCollection) {
      Iterator geomi = new GeometryCollectionIterator((GeometryCollection) geom);
      while (geomi.hasNext()) {
        Geometry g2 = (Geometry) geomi.next();
        if (g2 != geom)
          if (containsPoint(p, g2))
            return true;
      }
    }
    return false;
  }

  public static boolean containsPointInPolygon(Coordinates p, Polygon poly)
  {
    if (poly.isEmpty()) return false;
    LinearRing shell = (LinearRing) poly.getExteriorRing();
    if (! isPointInRing(p, shell)) return false;
    // now test if the point lies in or on the holes
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      LinearRing hole = (LinearRing) poly.getInteriorRing(i);
      if (isPointInRing(p, hole)) return false;
    }
    return true;
  }

  /**
   * Determines whether a point lies in a LinearRing,
   * using the ring envelope to short-circuit if possible.
   * 
   * @param p the point to test
   * @param ring a linear ring
   * @return true if the point lies inside the ring
   */
  private static boolean isPointInRing(Coordinates p, LinearRing ring)
  {
  	// short-circuit if point is not in ring envelope
  	if (! ring.getBoundingBox().intersects(p))
  		return false;
  	return CGAlgorithms.isPointInRing(p, ring.getCoordinateArray());
  }

	private Geometry geom;

	public SimplePointInAreaLocator(Geometry geom) {
		this.geom = geom;
	}

	public int locate(Coordinates p) {
		return SimplePointInAreaLocator.locate(p, geom);
	}

}
