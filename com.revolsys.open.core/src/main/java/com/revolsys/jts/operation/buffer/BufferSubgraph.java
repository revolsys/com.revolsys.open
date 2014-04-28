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
package com.revolsys.jts.operation.buffer;

/**
 * @version 1.7
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.TopologyException;
import com.revolsys.jts.geomgraph.DirectedEdge;
import com.revolsys.jts.geomgraph.DirectedEdgeStar;
import com.revolsys.jts.geomgraph.Node;
import com.revolsys.jts.geomgraph.Position;
import com.revolsys.jts.util.EnvelopeUtil;

//import debug.*;

/**
 * A connected subset of the graph of
 * {@link DirectedEdge}s and {@link Node}s.
 * Its edges will generate either
 * <ul>
 * <li> a single polygon in the complete buffer, with zero or more holes, or
 * <li> one or more connected holes
 * </ul>
 *
 *
 * @version 1.7
 */
class BufferSubgraph implements Comparable {
  private final RightmostEdgeFinder finder;

  private final List<DirectedEdge> dirEdgeList = new ArrayList<DirectedEdge>();

  private final List<Node> nodes = new ArrayList<>();

  private Coordinates rightMostCoord = null;

  private BoundingBox env = null;

  public BufferSubgraph() {
    finder = new RightmostEdgeFinder();
  }

  /**
   * Adds the argument node and all its out edges to the subgraph
   * @param node the node to add
   * @param nodeStack the current set of nodes being traversed
   */
  private void add(final Node node, final Stack<Node> nodeStack) {
    node.setVisited(true);
    nodes.add(node);
    for (final Iterator i = ((DirectedEdgeStar)node.getEdges()).iterator(); i.hasNext();) {
      final DirectedEdge de = (DirectedEdge)i.next();
      dirEdgeList.add(de);
      final DirectedEdge sym = de.getSym();
      final Node symNode = sym.getNode();
      /**
       * NOTE: this is a depth-first traversal of the graph.
       * This will cause a large depth of recursion.
       * It might be better to do a breadth-first traversal.
       */
      if (!symNode.isVisited()) {
        nodeStack.push(symNode);
      }
    }
  }

  /**
   * Adds all nodes and edges reachable from this node to the subgraph.
   * Uses an explicit stack to avoid a large depth of recursion.
   *
   * @param node a node known to be in the subgraph
   */
  private void addReachable(final Node startNode) {
    final Stack<Node> nodeStack = new Stack<>();
    nodeStack.add(startNode);
    while (!nodeStack.empty()) {
      final Node node = nodeStack.pop();
      add(node, nodeStack);
    }
  }

  private void clearVisitedEdges() {
    for (final DirectedEdge de : dirEdgeList) {
      de.setVisited(false);
    }
  }

  /**
   * BufferSubgraphs are compared on the x-value of their rightmost Coordinate.
   * This defines a partial ordering on the graphs such that:
   * <p>
   * g1 >= g2 <==> Ring(g2) does not contain Ring(g1)
   * <p>
   * where Polygon(g) is the buffer polygon that is built from g.
   * <p>
   * This relationship is used to sort the BufferSubgraphs so that shells are guaranteed to
   * be built before holes.
   */
  @Override
  public int compareTo(final Object o) {
    final BufferSubgraph graph = (BufferSubgraph)o;
    if (this.rightMostCoord.getX() < graph.rightMostCoord.getX()) {
      return -1;
    }
    if (this.rightMostCoord.getX() > graph.rightMostCoord.getX()) {
      return 1;
    }
    return 0;
  }

  public void computeDepth(final int outsideDepth) {
    clearVisitedEdges();
    // find an outside edge to assign depth to
    final DirectedEdge de = finder.getEdge();
    // right side of line returned by finder is on the outside
    de.setEdgeDepths(Position.RIGHT, outsideDepth);
    copySymDepths(de);

    // computeNodeDepth(n, de);
    computeDepths(de);
  }

  /**
   * Compute depths for all dirEdges via breadth-first traversal of nodes in graph
   * @param startEdge edge to start processing with
   */
  // <FIX> MD - use iteration & queue rather than recursion, for speed and
  // robustness
  private void computeDepths(final DirectedEdge startEdge) {
    final Set<Node> nodesVisited = new HashSet<Node>();
    final LinkedList<Node> nodeQueue = new LinkedList<>();

    final Node startNode = startEdge.getNode();
    nodeQueue.addLast(startNode);
    nodesVisited.add(startNode);
    startEdge.setVisited(true);

    while (!nodeQueue.isEmpty()) {
      // System.out.println(nodes.size() + " queue: " + nodeQueue.size());
      final Node n = nodeQueue.removeFirst();
      nodesVisited.add(n);
      // compute depths around node, starting at this edge since it has depths
      // assigned
      computeNodeDepth(n);

      // add all adjacent nodes to process queue,
      // unless the node has been visited already
      for (final Iterator i = ((DirectedEdgeStar)n.getEdges()).iterator(); i.hasNext();) {
        final DirectedEdge de = (DirectedEdge)i.next();
        final DirectedEdge sym = de.getSym();
        if (sym.isVisited()) {
          continue;
        }
        final Node adjNode = sym.getNode();
        if (!(nodesVisited.contains(adjNode))) {
          nodeQueue.addLast(adjNode);
          nodesVisited.add(adjNode);
        }
      }
    }
  }

  private void computeNodeDepth(final Node n) {
    // find a visited dirEdge to start at
    DirectedEdge startEdge = null;
    for (final Iterator i = ((DirectedEdgeStar)n.getEdges()).iterator(); i.hasNext();) {
      final DirectedEdge de = (DirectedEdge)i.next();
      if (de.isVisited() || de.getSym().isVisited()) {
        startEdge = de;
        break;
      }
    }
    // MD - testing Result: breaks algorithm
    // if (startEdge == null) return;

    // only compute string append if assertion would fail
    if (startEdge == null) {
      throw new TopologyException("unable to find edge to compute depths at "
        + n.getCoordinate());
    }

    ((DirectedEdgeStar)n.getEdges()).computeDepths(startEdge);

    // copy depths to sym edges
    for (final Iterator i = ((DirectedEdgeStar)n.getEdges()).iterator(); i.hasNext();) {
      final DirectedEdge de = (DirectedEdge)i.next();
      de.setVisited(true);
      copySymDepths(de);
    }
  }

  private void copySymDepths(final DirectedEdge de) {
    final DirectedEdge sym = de.getSym();
    sym.setDepth(Position.LEFT, de.getDepth(Position.RIGHT));
    sym.setDepth(Position.RIGHT, de.getDepth(Position.LEFT));
  }

  /**
   * Creates the subgraph consisting of all edges reachable from this node.
   * Finds the edges in the graph and the rightmost coordinate.
   *
   * @param node a node to start the graph traversal from
   */
  public void create(final Node node) {
    addReachable(node);
    finder.findEdge(dirEdgeList);
    rightMostCoord = finder.getCoordinate();
  }

  /**
   * Find all edges whose depths indicates that they are in the result area(s).
   * Since we want polygon shells to be
   * oriented CW, choose dirEdges with the interior of the result on the RHS.
   * Mark them as being in the result.
   * Interior Area edges are the result of dimensional collapses.
   * They do not form part of the result area boundary.
   */
  public void findResultEdges() {
    for (final DirectedEdge de : dirEdgeList) {
      /**
       * Select edges which have an interior depth on the RHS
       * and an exterior depth on the LHS.
       * Note that because of weird rounding effects there may be
       * edges which have negative depths!  Negative depths
       * count as "outside".
       */
      // <FIX> - handle negative depths
      final int depthRight = de.getDepth(Position.RIGHT);
      if (depthRight >= 1) {
        final int depthLeft = de.getDepth(Position.LEFT);
        if (depthLeft <= 0) {
          final boolean interiorAreaEdge = de.isInteriorAreaEdge();
          if (!interiorAreaEdge) {
            de.setInResult(true);
          }
        }
      }
    }
  }

  public List<DirectedEdge> getDirectedEdges() {
    return dirEdgeList;
  }

  /**
   * Computes the envelope of the edges in the subgraph.
   * The envelope is cached after being computed.
   *
   * @return the envelope of the graph.
   */
  public BoundingBox getEnvelope() {
    if (env == null) {
      double[] bounds = null;
      for (final DirectedEdge dirEdge : dirEdgeList) {
        final Coordinates[] pts = dirEdge.getEdge().getCoordinates();
        for (final Coordinates point : pts) {
          if (bounds == null) {
            bounds = EnvelopeUtil.createBounds(2, point);
          } else {
            EnvelopeUtil.expand(bounds, 2, point);
          }
        }
      }
      env = new Envelope(2, bounds);
    }
    return env;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  /**
   * Gets the rightmost coordinate in the edges of the subgraph
   */
  public Coordinates getRightmostCoordinate() {
    return rightMostCoord;
  }

  /*
   * // DEBUGGING only - comment out private static final String SAVE_DIREDGES =
   * "saveDirEdges"; private static int saveCount = 0; public void
   * saveDirEdges() { GeometryFactory fact = GeometryFactory.getFactory(); for
   * (Iterator it = dirEdgeList.iterator(); it.hasNext(); ) { DirectedEdge de =
   * (DirectedEdge) it.next(); double dx = de.getDx(); double dy = de.getDy();
   * Coordinates p0 = de.getCoordinate(); double ang = Math.atan2(dy, dx);
   * Coordinates p1 = new Coordinate((double) p0.x + .4 * Math.cos(ang), p0.y +
   * .4 * Math.sin(ang)); // DebugFeature.add(SAVE_DIREDGES, //
   * fact.createLineString(new Coordinates[] { p0, p1 } ), //
   * de.getDepth(Position.LEFT) + "/" + de.getDepth(Position.RIGHT) // ); }
   * String filepath = "x:\\jts\\testBuffer\\dirEdges" + saveCount++ + ".jml";
   * DebugFeature.saveFeatures(SAVE_DIREDGES, filepath); }
   */
}
