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
package com.revolsys.jtstest.clean;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;

/**
 * @version 1.7
 */
public class CleanDuplicatePoints {

  public static Coordinates[] removeDuplicatePoints(final Coordinates[] coord) {
    final List uniqueCoords = new ArrayList();
    Coordinates lastPt = null;
    for (int i = 0; i < coord.length; i++) {
      if (lastPt == null || !lastPt.equals(coord[i])) {
        lastPt = coord[i];
        uniqueCoords.add(new Coordinate(lastPt));
      }
    }
    return (Coordinates[])uniqueCoords.toArray(new Coordinates[0]);
  }

  private GeometryFactory fact;

  public CleanDuplicatePoints() {
  }

  public Geometry clean(final Geometry g) {
    fact = g.getGeometryFactory();
    if (g.isEmpty()) {
      return g;
    }
    if (g instanceof Point) {
      return g;
    } else if (g instanceof MultiPoint) {
      return g;
    } else if (g instanceof LinearRing) {
      return clean((LinearRing)g);
    } else if (g instanceof LineString) {
      return clean((LineString)g);
    } else if (g instanceof Polygon) {
      return clean((Polygon)g);
    } else if (g instanceof MultiLineString) {
      return clean((MultiLineString)g);
    } else if (g instanceof MultiPolygon) {
      return clean((MultiPolygon)g);
    } else if (g instanceof GeometryCollection) {
      return clean((GeometryCollection)g);
    } else {
      throw new UnsupportedOperationException(g.getClass().getName());
    }
  }

  private GeometryCollection clean(final GeometryCollection g) {
    final List geoms = new ArrayList();
    for (int i = 0; i < g.getGeometryCount(); i++) {
      final Geometry geom = g.getGeometry(i);
      geoms.add(clean(geom));
    }
    return fact.geometryCollection(geoms);
  }

  private LinearRing clean(final LinearRing g) {
    final Coordinates[] coords = removeDuplicatePoints(g.getCoordinateArray());
    return fact.linearRing(coords);
  }

  private LineString clean(final LineString g) {
    final Coordinates[] coords = removeDuplicatePoints(g.getCoordinateArray());
    return fact.lineString(coords);
  }

  private MultiLineString clean(final MultiLineString g) {
    final List lines = new ArrayList();
    for (int i = 0; i < g.getGeometryCount(); i++) {
      final LineString line = (LineString)g.getGeometry(i);
      lines.add(clean(line));
    }
    return fact.multiLineString(lines);
  }

  private MultiPolygon clean(final MultiPolygon g) {
    final List polys = new ArrayList();
    for (int i = 0; i < g.getGeometryCount(); i++) {
      final Polygon poly = (Polygon)g.getGeometry(i);
      polys.add(clean(poly));
    }
    return fact.multiPolygon(polys);
  }

  private Polygon clean(final Polygon poly) {
    final Coordinates[] shellCoords = removeDuplicatePoints(poly.getExteriorRing()
      .getCoordinateArray());
    final LinearRing shell = fact.linearRing(shellCoords);
    final List<LinearRing> rings = new ArrayList<>();
    rings.add(shell);
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      final Coordinates[] holeCoords = removeDuplicatePoints(poly.getInteriorRing(
        i)
        .getCoordinateArray());
      rings.add(fact.linearRing(holeCoords));
    }
    return fact.polygon(rings);
  }

}
