
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
package com.revolsys.jts.operation.overlay;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.geomgraph.Edge;
import com.revolsys.jts.geomgraph.index.EdgeSetIntersector;
import com.revolsys.jts.geomgraph.index.SegmentIntersector;
import com.revolsys.jts.geomgraph.index.SimpleMCSweepLineIntersector;

/**
 * Nodes a set of edges.
 * Takes one or more sets of edges and constructs a
 * new set of edges consisting of all the split edges created by
 * noding the input edges together
 * @version 1.7
 */
public class EdgeSetNoder {

  private final LineIntersector li;

  private final List inputEdges = new ArrayList();

  public EdgeSetNoder(final LineIntersector li) {
    this.li = li;
  }

  public void addEdges(final List edges) {
    this.inputEdges.addAll(edges);
  }

  public List getNodedEdges() {
    final EdgeSetIntersector esi = new SimpleMCSweepLineIntersector();
    final SegmentIntersector si = new SegmentIntersector(this.li, true, false);
    esi.computeIntersections(this.inputEdges, si, true);
    // Debug.println("has proper int = " + si.hasProperIntersection());

    final List splitEdges = new ArrayList();
    for (final Iterator i = this.inputEdges.iterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      e.getEdgeIntersectionList().addSplitEdges(splitEdges);
    }
    return splitEdges;
  }
}
