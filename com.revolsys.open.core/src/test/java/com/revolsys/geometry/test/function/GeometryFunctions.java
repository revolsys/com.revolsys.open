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
package com.revolsys.geometry.test.function;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;

/**
 * Implementations for various geometry functions.
 *
 * @author Martin Davis
 *
 */
public class GeometryFunctions {
  public static String lengthDescription = "Computes the length of perimeter of a Geometry";

  public static double area(final Geometry g) {
    return g.getArea();
  }

  public static Geometry convertToPolygon(final Geometry g) {
    if (g instanceof Polygonal) {
      return g;
    }
    // TODO: ensure ring is valid
    final LinearRing ring = g.getGeometryFactory()
      .linearRing(CoordinatesListUtil.getCoordinateArray(g));
    return g.getGeometryFactory().polygon(ring);
  }

  public static Geometry envelope(final Geometry g) {
    return g.getEnvelope();
  }

  public static Geometry getCoordinates(final Geometry g) {
    final Point[] pts = CoordinatesListUtil.getCoordinateArray(g);
    return g.getGeometryFactory().punctual(pts);
  }

  public static Geometry getGeometry(final Geometry g, final int i) {
    return g.getGeometry(i);
  }

  public static Geometry getPolygonHoleN(final Geometry g, final int i) {
    if (g instanceof Polygon) {
      final LinearRing ring = ((Polygon)g).getHole(i);
      return ring;
    }
    return null;
  }

  public static Geometry getPolygonShell(final Geometry g) {
    if (g instanceof Polygon) {
      final LinearRing shell = ((Polygon)g).getShell();
      return g.getGeometryFactory().polygon(shell);
    } else if (g instanceof Polygonal) {
      final Polygon[] poly = new Polygon[g.getGeometryCount()];
      for (int i = 0; i < g.getGeometryCount(); i++) {
        final LinearRing shell = ((Polygon)g.getGeometry(i)).getShell();
        poly[i] = g.getGeometryFactory().polygon(shell);
      }
      return g.getGeometryFactory().polygonal(poly);
    }
    return null;
  }

  public static boolean isCCW(final Geometry g) {
    if (g instanceof Polygon) {
      return ((Polygon)g).getShell().isCounterClockwise();
    } else if (g instanceof LineString && ((LineString)g).isClosed()) {
      return ((LineString)g).isCounterClockwise();
    } else {
      return false;
    }
  }

  public static boolean isClosed(final Geometry g) {
    if (g instanceof Lineal) {
      return ((Lineal)g).isClosed();
    }
    // other geometry types are defined to be closed
    return true;
  }

  public static boolean isRectangle(final Geometry g) {
    return g.isRectangle();
  }

  public static boolean isSimple(final Geometry g) {
    return g.isSimple();
  }

  public static boolean isValid(final Geometry g) {
    return g.isValid();
  }

  public static double length(final Geometry g) {
    return g.getLength();
  }

  public static Geometry normalize(final Geometry g) {
    final Geometry gNorm = g.normalize();
    return gNorm;
  }

  public static Geometry reverse(final Geometry g) {
    return g.reverse();
  }
}
