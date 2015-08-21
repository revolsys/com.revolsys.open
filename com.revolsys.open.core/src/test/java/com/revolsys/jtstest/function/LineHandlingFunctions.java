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
package com.revolsys.jtstest.function;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.geometry.dissolve.LineDissolver;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.operation.linemerge.LineSequencer;
import com.revolsys.gis.graph.linemerge.LineMerger;

public class LineHandlingFunctions {

  public static Geometry dissolve(final Geometry geom) {
    return LineDissolver.dissolve(geom);
  }

  private static LineString extractChain(final LineString line, final int index,
    final int maxChainSize) {
    int size = maxChainSize + 1;
    if (index + size > line.getVertexCount()) {
      size = line.getVertexCount() - index;
    }
    final Point[] pts = new Point[size];
    for (int i = 0; i < size; i++) {
      pts[i] = line.getPoint(index + i);
    }
    return line.getGeometryFactory().lineString(pts);
  }

  public static Geometry extractChains(final Geometry g, final int maxChainSize) {
    final List lines = g.getGeometries(LineString.class);
    final List chains = new ArrayList();
    for (final Iterator it = lines.iterator(); it.hasNext();) {
      final LineString line = (LineString)it.next();
      for (int i = 0; i < line.getVertexCount() - 1; i += maxChainSize) {
        final LineString chain = extractChain(line, i, maxChainSize);
        chains.add(chain);
      }
    }
    return g.getGeometryFactory().buildGeometry(chains);
  }

  public static Geometry extractLines(final Geometry g) {
    final List lines = g.getGeometries(LineString.class);
    return g.getGeometryFactory().buildGeometry(lines);
  }

  public static Geometry extractSegments(final Geometry g) {
    final List lines = g.getGeometries(LineString.class);
    final List segments = new ArrayList();
    for (final Iterator it = lines.iterator(); it.hasNext();) {
      final LineString line = (LineString)it.next();
      for (int i = 1; i < line.getVertexCount(); i++) {
        final LineString seg = g.getGeometryFactory().lineString(new Point[] {
          line.getPoint(i - 1), line.getPoint(i)
        });
        segments.add(seg);
      }
    }
    return g.getGeometryFactory().buildGeometry(segments);
  }

  public static Geometry mergeLines(final Geometry g) {
    final Collection<LineString> lines = LineMerger.merge(g);
    return g.getGeometryFactory().buildGeometry(lines);
  }

  public static Geometry sequenceLines(final Geometry g) {
    return LineSequencer.sequence(g);
  }

}
