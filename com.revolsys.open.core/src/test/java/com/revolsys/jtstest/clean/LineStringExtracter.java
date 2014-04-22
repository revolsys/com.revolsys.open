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

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Polygon;

/**
 * @version 1.7
 */
public class LineStringExtracter {

  private GeometryFactory fact;

  public LineStringExtracter() {
  }

  private void addLines(final Geometry g, final List lines) {
    if (g.isEmpty()) {
      return;
    } else if (g instanceof LineString) {
      lines.add(g);
    } else if (g instanceof Polygon) {
      addLines((Polygon)g, lines);
    } else if (g instanceof GeometryCollection) {
      addLines((GeometryCollection)g, lines);
    }
  }

  private void addLines(final GeometryCollection g, final List lines) {
    for (int i = 0; i < g.getGeometryCount(); i++) {
      final Geometry geom = g.getGeometry(i);
      addLines(geom, lines);
    }
  }

  private void addLines(final Polygon poly, final List lines) {
    lines.add(fact.lineString(poly.getExteriorRing().getCoordinateArray()));
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      lines.add(fact.lineString(poly.getInteriorRing(i).getCoordinateArray()));
    }
  }

  public Geometry extract(final Geometry g) {
    fact = g.getGeometryFactory();
    final List lines = new ArrayList();
    addLines(g, lines);
    return fact.buildGeometry(lines);
  }

}
