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

import com.revolsys.jts.geom.CoordinateArrays;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.planargraph.DirectedEdge;
import com.revolsys.jts.planargraph.Edge;
import com.revolsys.jts.planargraph.Node;
import com.revolsys.jts.planargraph.PlanarGraph;

/**
 * A planar graph of edges that is analyzed to sew the edges together. The 
 * <code>marked</code> flag on @{link com.vividsolutions.planargraph.Edge}s 
 * and @{link com.vividsolutions.planargraph.Node}s indicates whether they have been
 * logically deleted from the graph.
 *
 * @version 1.7
 */
public class LineMergeGraph extends PlanarGraph {
  /**
   * Adds an Edge, DirectedEdges, and Nodes for the given LineString representation
   * of an edge. 
   * Empty lines or lines with all coordinates equal are not added.
   * 
   * @param lineString the linestring to add to the graph
   */
  public void addEdge(final LineString lineString) {
    if (lineString.isEmpty()) {
      return;
    }

    final Coordinates[] coordinates = CoordinateArrays.removeRepeatedPoints(lineString.getCoordinateArray());

    // don't add lines with all coordinates equal
    if (coordinates.length <= 1) {
      return;
    }

    final Coordinates startCoordinate = coordinates[0];
    final Coordinates endCoordinate = coordinates[coordinates.length - 1];
    final Node startNode = getNode(startCoordinate);
    final Node endNode = getNode(endCoordinate);
    final DirectedEdge directedEdge0 = new LineMergeDirectedEdge(startNode,
      endNode, coordinates[1], true);
    final DirectedEdge directedEdge1 = new LineMergeDirectedEdge(endNode,
      startNode, coordinates[coordinates.length - 2], false);
    final Edge edge = new LineMergeEdge(lineString);
    edge.setDirectedEdges(directedEdge0, directedEdge1);
    add(edge);
  }

  private Node getNode(final Coordinates coordinate) {
    Node node = findNode(coordinate);
    if (node == null) {
      node = new Node(coordinate);
      add(node);
    }

    return node;
  }
}
