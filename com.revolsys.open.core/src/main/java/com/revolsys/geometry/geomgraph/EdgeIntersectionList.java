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
package com.revolsys.geometry.geomgraph;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.LineStringDouble;

/**
 * A list of edge intersections along an {@link Edge}.
 * Implements splitting an edge with intersections
 * into multiple resultant edges.
 *
 * @version 1.7
 */
public class EdgeIntersectionList implements Iterable<EdgeIntersection> {
  Edge edge; // the parent edge

  // a Map <EdgeIntersection, EdgeIntersection>
  private final Map<EdgeIntersection, EdgeIntersection> nodeMap = new TreeMap<>();

  public EdgeIntersectionList(final Edge edge) {
    this.edge = edge;
  }

  /**
   * Adds an intersection into the list, if it isn't already there.
   * The input segmentIndex and dist are expected to be normalized.
   * @return the EdgeIntersection found or added
   */
  public EdgeIntersection add(final Point intPt, final int segmentIndex, final double dist) {
    final EdgeIntersection eiNew = new EdgeIntersection(intPt, segmentIndex, dist);
    final EdgeIntersection ei = this.nodeMap.get(eiNew);
    if (ei != null) {
      return ei;
    }
    this.nodeMap.put(eiNew, eiNew);
    return eiNew;
  }

  /**
   * Adds entries for the first and last points of the edge to the list
   */
  public void addEndpoints() {
    final int maxSegIndex = this.edge.getVertexCount() - 1;
    add(this.edge.getPoint(0), 0, 0.0);
    add(this.edge.getPoint(maxSegIndex), maxSegIndex, 0.0);
  }

  /**
   * Creates new edges for all the edges that the intersections in this
   * list split the parent edge into.
   * Adds the edges to the input list (this is so a single list
   * can be used to accumulate all split edges for a Geometry).
   *
   * @param edgeList a list of EdgeIntersections
   */
  public void addSplitEdges(final List<Edge> edgeList) {
    // ensure that the list has entries for the first and last point of the edge
    addEndpoints();

    final Iterator<EdgeIntersection> it = iterator();
    // there should always be at least two entries in the list
    EdgeIntersection eiPrev = it.next();
    while (it.hasNext()) {
      final EdgeIntersection ei = it.next();
      final Edge newEdge = newSplitEdge(eiPrev, ei);
      edgeList.add(newEdge);

      eiPrev = ei;
    }
  }

  /**
   * Tests if the given point is an edge intersection
   *
   * @param pt the point to test
   * @return true if the point is an intersection
   */
  public boolean isIntersection(final Point pt) {
    for (final Object element : this) {
      final EdgeIntersection ei = (EdgeIntersection)element;
      if (ei.coord.equals(pt)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns an iterator of {@link EdgeIntersection}s
   *
   * @return an Iterator of EdgeIntersections
   */
  @Override
  public Iterator<EdgeIntersection> iterator() {
    return this.nodeMap.values().iterator();
  }

  /**
   * Construct a new new "split edge" with the section of points between
   * (and including) the two intersections.
   * The label for the new edge is the same as the label for the parent edge.
   */
  Edge newSplitEdge(final EdgeIntersection ei0, final EdgeIntersection ei1) {
    // Debug.print("\ncreateSplitEdge"); Debug.print(ei0); Debug.print(ei1);
    int npts = ei1.segmentIndex - ei0.segmentIndex + 2;

    final Point lastSegStartPt = this.edge.getPoint(ei1.segmentIndex);
    // if the last intersection point is not equal to the its segment start pt,
    // add it to the points list as well.
    // (This check is needed because the distance metric is not totally
    // reliable!)
    // The check for point equality is 2D only - Z values are ignored
    final boolean useIntPt1 = ei1.dist > 0.0 || !ei1.coord.equals(2, lastSegStartPt);
    if (!useIntPt1) {
      npts--;
    }

    final Point[] pts = new Point[npts];
    int ipt = 0;
    pts[ipt++] = ei0.coord;
    for (int i = ei0.segmentIndex + 1; i <= ei1.segmentIndex; i++) {
      pts[ipt++] = this.edge.getPoint(i);
    }
    if (useIntPt1) {
      pts[ipt] = ei1.coord;
    }
    final LineStringDouble points = new LineStringDouble(pts);
    return new Edge(points, new Label(this.edge.label));
  }

  public void print(final PrintStream out) {
    out.println("Intersections:");
    for (final Object element : this) {
      final EdgeIntersection ei = (EdgeIntersection)element;
      ei.print(out);
    }
  }
}
