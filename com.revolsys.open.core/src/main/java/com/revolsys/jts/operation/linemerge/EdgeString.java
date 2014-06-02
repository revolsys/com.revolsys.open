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
package com.revolsys.jts.operation.linemerge;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.geom.CoordinateList;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;

/**
 * A sequence of {@link LineMergeDirectedEdge}s forming one of the lines that will
 * be output by the line-merging process.
 *
 * @version 1.7
 */
public class EdgeString {
  private final GeometryFactory factory;

  private final List<LineMergeDirectedEdge> directedEdges = new ArrayList<>();

  private LineString line = null;

  /**
   * Constructs an EdgeString with the given factory used to convert this EdgeString
   * to a LineString
   */
  public EdgeString(final GeometryFactory factory) {
    this.factory = factory;
  }

  /**
   * Adds a directed edge which is known to form part of this line.
   */
  public void add(final LineMergeDirectedEdge directedEdge) {
    directedEdges.add(directedEdge);
  }

  /**
   * Converts this EdgeString into a LineString.
   */
  public LineString toLineString() {
    if (line == null) {
      int forwardDirectedEdges = 0;
      int reverseDirectedEdges = 0;
      final CoordinateList coordinateList = new CoordinateList();
      for (final LineMergeDirectedEdge directedEdge : directedEdges) {
        if (directedEdge.getEdgeDirection()) {
          forwardDirectedEdges++;
        } else {
          reverseDirectedEdges++;
        }
        final LineMergeEdge edge = (LineMergeEdge)directedEdge.getEdge();
        coordinateList.add(edge.getLine(), false,
          directedEdge.getEdgeDirection());
      }
      line = factory.lineString(coordinateList);
      if (reverseDirectedEdges > forwardDirectedEdges) {
        line = line.reverse();
      }
    }
    return line;
  }
}
