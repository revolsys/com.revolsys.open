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
package com.revolsys.gis.model.geometry.operation.chain;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.util.Assert;

// INCOMPLETE!
class NodeVertexIterator implements Iterator {
  private final SegmentNodeList nodeList;

  private final NodedSegmentString edge;

  private final Iterator nodeIt;

  private SegmentNode currNode = null;

  private SegmentNode nextNode = null;

  private int currSegIndex = 0;

  NodeVertexIterator(final SegmentNodeList nodeList) {
    this.nodeList = nodeList;
    edge = nodeList.getEdge();
    nodeIt = nodeList.iterator();
    readNextNode();
  }

  @Override
  public boolean hasNext() {
    if (nextNode == null) {
      return false;
    }
    return true;
  }

  @Override
  public Object next() {
    if (currNode == null) {
      currNode = nextNode;
      currSegIndex = currNode.segmentIndex;
      readNextNode();
      return currNode;
    }
    // check for trying to read too far
    if (nextNode == null) {
      return null;
    }

    if (nextNode.segmentIndex == currNode.segmentIndex) {
      currNode = nextNode;
      currSegIndex = currNode.segmentIndex;
      readNextNode();
      return currNode;
    }

    if (nextNode.segmentIndex > currNode.segmentIndex) {

    }
    return null;
  }

  private void readNextNode() {
    if (nodeIt.hasNext()) {
      nextNode = (SegmentNode)nodeIt.next();
    } else {
      nextNode = null;
    }
  }

  /**
   * Not implemented.
   * 
   * @throws UnsupportedOperationException This method is not implemented.
   */
  @Override
  public void remove() {
    throw new UnsupportedOperationException(getClass().getName());
  }

}

/**
 * A list of the {@link SegmentNode}s present along a noded
 * {@link SegmentString}.
 * 
 * @version 1.7
 */
public class SegmentNodeList {
  private final Map nodeMap = new TreeMap();

  private final NodedSegmentString edge; // the parent edge

  public SegmentNodeList(final NodedSegmentString edge) {
    this.edge = edge;
  }

  /**
   * Adds an intersection into the list, if it isn't already there. The input
   * segmentIndex and dist are expected to be normalized.
   * 
   * @return the SegmentIntersection found or added
   */
  public SegmentNode add(final Coordinates intPt, final int segmentIndex) {
    final SegmentNode eiNew = new SegmentNode(edge, intPt, segmentIndex,
      edge.getSegmentOctant(segmentIndex));
    final SegmentNode ei = (SegmentNode)nodeMap.get(eiNew);
    if (ei != null) {
      // debugging sanity check
      Assert.isTrue(ei.coord.equals2d(intPt),
        "Found equal nodes with different coordinates");
      // if (! ei.coord.equals2d(intPt))
      // Debug.println("Found equal nodes with different coordinates");

      return ei;
    }
    // node does not exist, so create it
    nodeMap.put(eiNew, eiNew);
    return eiNew;
  }

  /**
   * Adds nodes for any collapsed edge pairs. Collapsed edge pairs can be caused
   * by inserted nodes, or they can be pre-existing in the edge vertex list. In
   * order to provide the correct fully noded semantics, the vertex at the base
   * of a collapsed pair must also be added as a node.
   */
  private void addCollapsedNodes() {
    final List collapsedVertexIndexes = new ArrayList();

    findCollapsesFromInsertedNodes(collapsedVertexIndexes);
    findCollapsesFromExistingVertices(collapsedVertexIndexes);

    // node the collapses
    for (final Iterator it = collapsedVertexIndexes.iterator(); it.hasNext();) {
      final int vertexIndex = ((Integer)it.next()).intValue();
      add(edge.getCoordinate(vertexIndex), vertexIndex);
    }
  }

  /**
   * Adds nodes for the first and last points of the edge
   */
  private void addEndpoints() {
    final int maxSegIndex = edge.size() - 1;
    add(edge.getCoordinate(0), 0);
    add(edge.getCoordinate(maxSegIndex), maxSegIndex);
  }

  /**
   * Creates new edges for all the edges that the intersections in this list
   * split the parent edge into. Adds the edges to the provided argument list
   * (this is so a single list can be used to accumulate all split edges for a
   * set of {@link SegmentString}s).
   */
  public void addSplitEdges(final Collection edgeList) {
    // ensure that the list has entries for the first and last point of the edge
    addEndpoints();
    addCollapsedNodes();

    final Iterator it = iterator();
    // there should always be at least two entries in the list, since the
    // endpoints are nodes
    SegmentNode eiPrev = (SegmentNode)it.next();
    while (it.hasNext()) {
      final SegmentNode ei = (SegmentNode)it.next();
      final SegmentString newEdge = createSplitEdge(eiPrev, ei);
      edgeList.add(newEdge);
      eiPrev = ei;
    }
    // checkSplitEdgesCorrectness(testingSplitEdges);
  }

  /**
   * Checks the correctness of the set of split edges corresponding to this edge
   * 
   * @param splitEdges the split edges for this edge (in order)
   */
  private void checkSplitEdgesCorrectness(final List splitEdges) {
    final CoordinatesList edgePts = edge.getCoordinates();

    // check that first and last points of split edges are same as endpoints of
    // edge
    final SegmentString split0 = (SegmentString)splitEdges.get(0);
    final Coordinates pt0 = split0.getCoordinate(0);
    if (!pt0.equals2d(edgePts.get(0))) {
      throw new RuntimeException("bad split edge start point at " + pt0);
    }

    final SegmentString splitn = (SegmentString)splitEdges.get(splitEdges.size() - 1);
    final CoordinatesList splitnPts = splitn.getCoordinates();
    final Coordinates ptn = splitnPts.get(splitnPts.size() - 1);
    if (!ptn.equals2d(edgePts.get(edgePts.size() - 1))) {
      throw new RuntimeException("bad split edge end point at " + ptn);
    }

  }

  /**
   * Create a new "split edge" with the section of points between (and
   * including) the two intersections. The label for the new edge is the same as
   * the label for the parent edge.
   */
  SegmentString createSplitEdge(final SegmentNode ei0, final SegmentNode ei1) {
    // Debug.println("\ncreateSplitEdge"); Debug.print(ei0); Debug.print(ei1);
    int npts = ei1.segmentIndex - ei0.segmentIndex + 2;

    final Coordinates lastSegStartPt = edge.getCoordinate(ei1.segmentIndex);
    // if the last intersection point is not equal to the its segment start pt,
    // add it to the points list as well.
    // (This check is needed because the distance metric is not totally
    // reliable!)
    // The check for point equality is 2D only - Z values are ignored
    final boolean useIntPt1 = ei1.isInterior()
      || !ei1.coord.equals2d(lastSegStartPt);
    if (!useIntPt1) {
      npts--;
    }

    final CoordinatesList pts = new DoubleCoordinatesList(npts, 2);
    int ipt = 0;
    pts.setPoint(ipt++, new DoubleCoordinates(ei0.coord));
    for (int i = ei0.segmentIndex + 1; i <= ei1.segmentIndex; i++) {
      pts.setPoint(ipt++, edge.getCoordinate(i));
    }
    if (useIntPt1) {
      pts.setPoint(ipt, ei1.coord);
    }

    return new NodedSegmentString(pts, edge.getData());
  }

  private boolean findCollapseIndex(final SegmentNode ei0,
    final SegmentNode ei1, final int[] collapsedVertexIndex) {
    // only looking for equal nodes
    if (!ei0.coord.equals2d(ei1.coord)) {
      return false;
    }

    int numVerticesBetween = ei1.segmentIndex - ei0.segmentIndex;
    if (!ei1.isInterior()) {
      numVerticesBetween--;
    }

    // if there is a single vertex between the two equal nodes, this is a
    // collapse
    if (numVerticesBetween == 1) {
      collapsedVertexIndex[0] = ei0.segmentIndex + 1;
      return true;
    }
    return false;
  }

  /**
   * Adds nodes for any collapsed edge pairs which are pre-existing in the
   * vertex list.
   */
  private void findCollapsesFromExistingVertices(
    final List collapsedVertexIndexes) {
    for (int i = 0; i < edge.size() - 2; i++) {
      final Coordinates p0 = edge.getCoordinate(i);
      final Coordinates p1 = edge.getCoordinate(i + 1);
      final Coordinates p2 = edge.getCoordinate(i + 2);
      if (p0.equals2d(p2)) {
        // add base of collapse as node
        collapsedVertexIndexes.add(new Integer(i + 1));
      }
    }
  }

  /**
   * Adds nodes for any collapsed edge pairs caused by inserted nodes Collapsed
   * edge pairs occur when the same coordinate is inserted as a node both before
   * and after an existing edge vertex. To provide the correct fully noded
   * semantics, the vertex must be added as a node as well.
   */
  private void findCollapsesFromInsertedNodes(final List collapsedVertexIndexes) {
    final int[] collapsedVertexIndex = new int[1];
    final Iterator it = iterator();
    // there should always be at least two entries in the list, since the
    // endpoints are nodes
    SegmentNode eiPrev = (SegmentNode)it.next();
    while (it.hasNext()) {
      final SegmentNode ei = (SegmentNode)it.next();
      final boolean isCollapsed = findCollapseIndex(eiPrev, ei,
        collapsedVertexIndex);
      if (isCollapsed) {
        collapsedVertexIndexes.add(new Integer(collapsedVertexIndex[0]));
      }

      eiPrev = ei;
    }
  }

  public NodedSegmentString getEdge() {
    return edge;
  }

  /**
   * returns an iterator of SegmentNodes
   */
  public Iterator iterator() {
    return nodeMap.values().iterator();
  }

  public void print(final PrintStream out) {
    out.println("Intersections:");
    for (final Iterator it = iterator(); it.hasNext();) {
      final SegmentNode ei = (SegmentNode)it.next();
      ei.print(out);
    }
  }
}
