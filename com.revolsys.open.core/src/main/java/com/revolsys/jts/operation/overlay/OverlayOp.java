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

import com.revolsys.jts.algorithm.PointLocator;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.TopologyException;
import com.revolsys.jts.geomgraph.Depth;
import com.revolsys.jts.geomgraph.DirectedEdge;
import com.revolsys.jts.geomgraph.DirectedEdgeStar;
import com.revolsys.jts.geomgraph.Edge;
import com.revolsys.jts.geomgraph.EdgeList;
import com.revolsys.jts.geomgraph.EdgeNodingValidator;
import com.revolsys.jts.geomgraph.Label;
import com.revolsys.jts.geomgraph.Node;
import com.revolsys.jts.geomgraph.PlanarGraph;
import com.revolsys.jts.geomgraph.Position;
import com.revolsys.jts.operation.GeometryGraphOperation;
import com.revolsys.jts.util.Assert;

/**
 * Computes the geometric overlay of two {@link Geometry}s.  The overlay
 * can be used to determine any boolean combination of the geometries.
 *
 * @version 1.7
 */
public class OverlayOp extends GeometryGraphOperation {
  /**
   * The spatial functions supported by this class.
   * These operations implement various boolean combinations of the resultants of the overlay.
   */

  /**
   * The code for the Intersection overlay operation.
   */
  public static final int INTERSECTION = 1;

  /**
   * The code for the Union overlay operation.
   */
  public static final int UNION = 2;

  /**
   *  The code for the Difference overlay operation.
   */
  public static final int DIFFERENCE = 3;

  /**
   *  The code for the Symmetric Difference overlay operation.
   */
  public static final int SYMDIFFERENCE = 4;

  /**
   * Creates an empty result geometry of the appropriate dimension,
   * based on the given overlay operation and the dimensions of the inputs.
   * The created geometry is always an atomic geometry, 
   * not a collection.
   * <p>
   * The empty result is constructed using the following rules:
   * <ul>
   * <li>{@link #INTERSECTION} - result has the dimension of the lowest input dimension
   * <li>{@link #UNION} - result has the dimension of the highest input dimension
   * <li>{@link #DIFFERENCE} - result has the dimension of the left-hand input
   * <li>{@link #SYMDIFFERENCE} - result has the dimension of the highest input dimension
   * (since the symmetric Difference is the union of the differences).
   * <li>
   * 
   * @param overlayOpCode the code for the overlay operation being performed
   * @param a an input geometry
   * @param b an input geometry
   * @param geomFact the geometry factory being used for the operation
   * @return an empty atomic geometry of the appropriate dimension
   */
  public static Geometry createEmptyResult(final int overlayOpCode,
    final Geometry a, final Geometry b, final GeometryFactory geomFact) {
    Geometry result = null;
    switch (resultDimension(overlayOpCode, a, b)) {
      case -1:
        result = geomFact.geometryCollection(new Geometry[0]);
      break;
      case 0:
        result = geomFact.point();
      break;
      case 1:
        result = geomFact.lineString();
      break;
      case 2:
        result = geomFact.polygon();
      break;
    }
    return result;
  }

  /**
   * Tests whether a point with a given topological {@link Label}
   * relative to two geometries is contained in 
   * the result of overlaying the geometries using
   * a given overlay operation.
   * <p>
   * The method handles arguments of {@link Location#NONE} correctly
   * 
   * @param label the topological label of the point
   * @param opCode the code for the overlay operation to test
   * @return true if the label locations correspond to the overlayOpCode
   */
  public static boolean isResultOfOp(final Label label, final int opCode) {
    final Location loc0 = label.getLocation(0);
    final Location loc1 = label.getLocation(1);
    return isResultOfOp(loc0, loc1, opCode);
  }

  /**
   * Tests whether a point with given {@link Location}s
   * relative to two geometries is contained in 
   * the result of overlaying the geometries using
   * a given overlay operation.
   * <p>
   * The method handles arguments of {@link Location#NONE} correctly
   *
   * @param loc0 the code for the location in the first geometry 
   * @param loc1 the code for the location in the second geometry 
   * @param overlayOpCode the code for the overlay operation to test
   * @return true if the locations correspond to the overlayOpCode
   */
  public static boolean isResultOfOp(Location loc0, Location loc1,
    final int overlayOpCode) {
    if (loc0 == Location.BOUNDARY) {
      loc0 = Location.INTERIOR;
    }
    if (loc1 == Location.BOUNDARY) {
      loc1 = Location.INTERIOR;
    }
    switch (overlayOpCode) {
      case INTERSECTION:
        return loc0 == Location.INTERIOR && loc1 == Location.INTERIOR;
      case UNION:
        return loc0 == Location.INTERIOR || loc1 == Location.INTERIOR;
      case DIFFERENCE:
        return loc0 == Location.INTERIOR && loc1 != Location.INTERIOR;
      case SYMDIFFERENCE:
        return (loc0 == Location.INTERIOR && loc1 != Location.INTERIOR)
          || (loc0 != Location.INTERIOR && loc1 == Location.INTERIOR);
    }
    return false;
  }

  /**
   * Computes an overlay operation for 
   * the given geometry arguments.
   * 
   * @param geom0 the first geometry argument
   * @param geom1 the second geometry argument
   * @param opCode the code for the desired overlay operation
   * @return the result of the overlay operation
   * @throws TopologyException if a robustness problem is encountered
   */
  public static Geometry overlayOp(final Geometry geom0, final Geometry geom1,
    final int opCode) {
    final OverlayOp gov = new OverlayOp(geom0, geom1);
    final Geometry geomOv = gov.getResultGeometry(opCode);
    return geomOv;
  }

  private static int resultDimension(final int opCode, final Geometry g0,
    final Geometry g1) {
    final int dim0 = g0.getDimension();
    final int dim1 = g1.getDimension();

    int resultDimension = -1;
    switch (opCode) {
      case INTERSECTION:
        resultDimension = Math.min(dim0, dim1);
      break;
      case UNION:
        resultDimension = Math.max(dim0, dim1);
      break;
      case DIFFERENCE:
        resultDimension = dim0;
      break;
      case SYMDIFFERENCE:
        /**
         * This result is chosen because
         * <pre>
         * SymDiff = Union(Diff(A, B), Diff(B, A)
         * </pre>
         * and Union has the dimension of the highest-dimension argument.
         */
        resultDimension = Math.max(dim0, dim1);
      break;
    }
    return resultDimension;
  }

  private final PointLocator ptLocator = new PointLocator();

  private final GeometryFactory geomFact;

  private Geometry resultGeom;

  private final PlanarGraph graph;

  private final EdgeList edgeList = new EdgeList();

  private List resultPolyList = new ArrayList();

  private List resultLineList = new ArrayList();

  private List resultPointList = new ArrayList();

  /**
   * Constructs an instance to compute a single overlay operation
   * for the given geometries.
   * 
   * @param g0 the first geometry argument
   * @param g1 the second geometry argument
   */
  public OverlayOp(final Geometry g0, final Geometry g1) {
    super(g0, g1);
    graph = new PlanarGraph(new OverlayNodeFactory());
    /**
     * Use factory of primary geometry.
     * Note that this does NOT handle mixed-precision arguments
     * where the second arg has greater precision than the first.
     */
    geomFact = g0.getGeometryFactory();
  }

  /**
   * If both a dirEdge and its sym are marked as being in the result, cancel
   * them out.
   */
  private void cancelDuplicateResultEdges() {
    // remove any dirEdges whose sym is also included
    // (they "cancel each other out")
    for (final Iterator it = graph.getEdgeEnds().iterator(); it.hasNext();) {
      final DirectedEdge de = (DirectedEdge)it.next();
      final DirectedEdge sym = de.getSym();
      if (de.isInResult() && sym.isInResult()) {
        de.setInResult(false);
        sym.setInResult(false);
        // Debug.print("cancelled "); Debug.println(de); Debug.println(sym);
      }
    }
  }

  private Geometry computeGeometry(final List resultPointList,
    final List resultLineList, final List resultPolyList, final int opcode) {
    final List geomList = new ArrayList();
    // element geometries of the result are always in the order P,L,A
    geomList.addAll(resultPointList);
    geomList.addAll(resultLineList);
    geomList.addAll(resultPolyList);

    // *
    if (geomList.isEmpty()) {
      return createEmptyResult(opcode, arg[0].getGeometry(),
        arg[1].getGeometry(), geomFact);
      // */
    }

    // build the most specific geometry possible
    return geomFact.buildGeometry(geomList);
  }

  /**
   * Compute initial labelling for all DirectedEdges at each node.
   * In this step, DirectedEdges will acquire a complete labelling
   * (i.e. one with labels for both Geometries)
   * only if they
   * are incident on a node which has edges for both Geometries
   */
  private void computeLabelling() {
    for (final Iterator nodeit = graph.getNodes().iterator(); nodeit.hasNext();) {
      final Node node = (Node)nodeit.next();
      // if (node.getCoordinate().equals(new Coordinate((double)222, 100)) )
      // Debug.addWatch(node.getEdges());
      node.getEdges().computeLabelling(arg);
    }
    mergeSymLabels();
    updateNodeLabelling();
  }

  /**
   * If either of the GeometryLocations for the existing label is
   * exactly opposite to the one in the labelToMerge,
   * this indicates a dimensional collapse has happened.
   * In this case, convert the label for that Geometry to a Line label
   */
  /*
   * NOT NEEDED? private void checkDimensionalCollapse(Label labelToMerge, Label
   * existingLabel) { if (existingLabel.isArea() && labelToMerge.isArea()) { for
   * (int i = 0; i < 2; i++) { if (! labelToMerge.isNull(i) &&
   * labelToMerge.getLocation(i, Position.LEFT) == existingLabel.getLocation(i,
   * Position.RIGHT) && labelToMerge.getLocation(i, Position.RIGHT) ==
   * existingLabel.getLocation(i, Position.LEFT) ) { existingLabel.toLine(i); }
   * } } }
   */
  /**
   * Update the labels for edges according to their depths.
   * For each edge, the depths are first normalized.
   * Then, if the depths for the edge are equal,
   * this edge must have collapsed into a line edge.
   * If the depths are not equal, update the label
   * with the locations corresponding to the depths
   * (i.e. a depth of 0 corresponds to a Location of EXTERIOR,
   * a depth of 1 corresponds to INTERIOR)
   */
  private void computeLabelsFromDepths() {
    for (final Iterator it = edgeList.iterator(); it.hasNext();) {
      final Edge e = (Edge)it.next();
      final Label lbl = e.getLabel();
      final Depth depth = e.getDepth();
      /**
       * Only check edges for which there were duplicates,
       * since these are the only ones which might
       * be the result of dimensional collapses.
       */
      if (!depth.isNull()) {
        depth.normalize();
        for (int i = 0; i < 2; i++) {
          if (!lbl.isNull(i) && lbl.isArea() && !depth.isNull(i)) {
            /**
             * if the depths are equal, this edge is the result of
             * the dimensional collapse of two or more edges.
             * It has the same location on both sides of the edge,
             * so it has collapsed to a line.
             */
            if (depth.getDelta(i) == 0) {
              lbl.toLine(i);
            } else {
              /**
               * This edge may be the result of a dimensional collapse,
               * but it still has different locations on both sides.  The
               * label of the edge must be updated to reflect the resultant
               * side locations indicated by the depth values.
               */
              Assert.isTrue(!depth.isNull(i, Position.LEFT),
                "depth of LEFT side has not been initialized");
              lbl.setLocation(i, Position.LEFT,
                depth.getLocation(i, Position.LEFT));
              Assert.isTrue(!depth.isNull(i, Position.RIGHT),
                "depth of RIGHT side has not been initialized");
              lbl.setLocation(i, Position.RIGHT,
                depth.getLocation(i, Position.RIGHT));
            }
          }
        }
      }
    }
  }

  private void computeOverlay(final int opCode) {
    // copy points from input Geometries.
    // This ensures that any Point geometries
    // in the input are considered for inclusion in the result set
    copyPoints(0);
    copyPoints(1);

    // node the input Geometries
    arg[0].computeSelfNodes(li, false);
    arg[1].computeSelfNodes(li, false);

    // compute intersections between edges of the two input geometries
    arg[0].computeEdgeIntersections(arg[1], li, true);

    final List baseSplitEdges = new ArrayList();
    arg[0].computeSplitEdges(baseSplitEdges);
    arg[1].computeSplitEdges(baseSplitEdges);
    final List splitEdges = baseSplitEdges;
    // add the noded edges to this result graph
    insertUniqueEdges(baseSplitEdges);

    computeLabelsFromDepths();
    replaceCollapsedEdges();

    // Debug.println(edgeList);

    /**
     * Check that the noding completed correctly.
     * 
     * This test is slow, but necessary in order to catch robustness failure 
     * situations.
     * If an exception is thrown because of a noding failure, 
     * then snapping will be performed, which will hopefully avoid the problem.
     * In the future hopefully a faster check can be developed.  
     * 
     */
    EdgeNodingValidator.checkValid(edgeList.getEdges());

    graph.addEdges(edgeList.getEdges());
    computeLabelling();
    // Debug.printWatch();
    labelIncompleteNodes();
    // Debug.printWatch();
    // nodeMap.print(System.out);

    /**
     * The ordering of building the result Geometries is important.
     * Areas must be built before lines, which must be built before points.
     * This is so that lines which are covered by areas are not included
     * explicitly, and similarly for points.
     */
    findResultAreaEdges(opCode);
    cancelDuplicateResultEdges();

    final PolygonBuilder polyBuilder = new PolygonBuilder(geomFact);
    polyBuilder.add(graph);
    resultPolyList = polyBuilder.getPolygons();

    final LineBuilder lineBuilder = new LineBuilder(this, geomFact, ptLocator);
    resultLineList = lineBuilder.build(opCode);

    final PointBuilder pointBuilder = new PointBuilder(this, geomFact,
      ptLocator);
    resultPointList = pointBuilder.build(opCode);

    // gather the results from all calculations into a single Geometry for the
    // result set
    resultGeom = computeGeometry(resultPointList, resultLineList,
      resultPolyList, opCode);
  }

  /**
   * Copy all nodes from an arg geometry into this graph.
   * The node label in the arg geometry overrides any previously computed
   * label for that argIndex.
   * (E.g. a node may be an intersection node with
   * a previously computed label of BOUNDARY,
   * but in the original arg Geometry it is actually
   * in the interior due to the Boundary Determination Rule)
   */
  private void copyPoints(final int argIndex) {
    for (final Iterator i = arg[argIndex].getNodeIterator(); i.hasNext();) {
      final Node graphNode = (Node)i.next();
      final Node newNode = graph.addNode(graphNode.getCoordinate());
      newNode.setLabel(argIndex, graphNode.getLabel().getLocation(argIndex));
    }
  }

  /**
   * Find all edges whose label indicates that they are in the result area(s),
   * according to the operation being performed.  Since we want polygon shells to be
   * oriented CW, choose dirEdges with the interior of the result on the RHS.
   * Mark them as being in the result.
   * Interior Area edges are the result of dimensional collapses.
   * They do not form part of the result area boundary.
   */
  private void findResultAreaEdges(final int opCode) {
    for (final Iterator it = graph.getEdgeEnds().iterator(); it.hasNext();) {
      final DirectedEdge de = (DirectedEdge)it.next();
      // mark all dirEdges with the appropriate label
      final Label label = de.getLabel();
      if (label.isArea()
        && !de.isInteriorAreaEdge()
        && isResultOfOp(label.getLocation(0, Position.RIGHT),
          label.getLocation(1, Position.RIGHT), opCode)) {
        de.setInResult(true);
        // Debug.print("in result "); Debug.println(de);
      }
    }
  }

  /**
   * Gets the graph constructed to compute the overlay.
   * 
   * @return the overlay graph
   */
  public PlanarGraph getGraph() {
    return graph;
  }

  /**
   * Gets the result of the overlay for a given overlay operation.
   * <p>
   * Note: this method can be called once only.
   * 
   * @param overlayOpCode the overlay operation to perform
   * @return the compute result geometry
   * @throws TopologyException if a robustness problem is encountered
   */
  public Geometry getResultGeometry(final int overlayOpCode) {
    computeOverlay(overlayOpCode);
    return resultGeom;
  }

  /**
   * Insert an edge from one of the noded input graphs.
   * Checks edges that are inserted to see if an
   * identical edge already exists.
   * If so, the edge is not inserted, but its label is merged
   * with the existing edge.
   */
  protected void insertUniqueEdge(final Edge e) {
    // <FIX> MD 8 Oct 03 speed up identical edge lookup
    // fast lookup
    final Edge existingEdge = edgeList.findEqualEdge(e);

    // If an identical edge already exists, simply update its label
    if (existingEdge != null) {
      final Label existingLabel = existingEdge.getLabel();

      Label labelToMerge = e.getLabel();
      // check if new edge is in reverse direction to existing edge
      // if so, must flip the label before merging it
      if (!existingEdge.isPointwiseEqual(e)) {
        labelToMerge = new Label(e.getLabel());
        labelToMerge.flip();
      }
      final Depth depth = existingEdge.getDepth();
      // if this is the first duplicate found for this edge, initialize the
      // depths
      // /*
      if (depth.isNull()) {
        depth.add(existingLabel);
      }
      // */
      depth.add(labelToMerge);
      existingLabel.merge(labelToMerge);
      // Debug.print("inserted edge: "); Debug.println(e);
      // Debug.print("existing edge: "); Debug.println(existingEdge);

    } else { // no matching existing edge was found
      // add this new edge to the list of edges in this graph
      // e.setName(name + edges.size());
      // e.getDepth().add(e.getLabel());
      edgeList.add(e);
    }
  }

  private void insertUniqueEdges(final List edges) {
    for (final Iterator i = edges.iterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      insertUniqueEdge(e);
    }
  }

  /**
   * @return true if the coord is located in the interior or boundary of
   * a geometry in the list.
   */
  private boolean isCovered(final Coordinates coord, final List geomList) {
    for (final Iterator it = geomList.iterator(); it.hasNext();) {
      final Geometry geom = (Geometry)it.next();
      final Location loc = ptLocator.locate(coord, geom);
      if (loc != Location.EXTERIOR) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tests if an L edge should be included in the result or not.
   *
   * @param coord the point coordinate
   * @return true if the coordinate point is covered by a result Area geometry
   */
  public boolean isCoveredByA(final Coordinates coord) {
    if (isCovered(coord, resultPolyList)) {
      return true;
    }
    return false;
  }

  /**
   * Tests if a point node should be included in the result or not.
   *
   * @param coord the point coordinate
   * @return true if the coordinate point is covered by a result Line or Area geometry
   */
  public boolean isCoveredByLA(final Coordinates coord) {
    if (isCovered(coord, resultLineList)) {
      return true;
    }
    if (isCovered(coord, resultPolyList)) {
      return true;
    }
    return false;
  }

  /**
   * Label an isolated node with its relationship to the target geometry.
   */
  private void labelIncompleteNode(final Node n, final int targetIndex) {
    final Location loc = ptLocator.locate(n.getCoordinate(),
      arg[targetIndex].getGeometry());

    // MD - 2008-10-24 - experimental for now
    // int loc = arg[targetIndex].locate(n.getCoordinate());
    n.getLabel().setLocation(targetIndex, loc);
  }

  /**
   * Incomplete nodes are nodes whose labels are incomplete.
   * (e.g. the location for one Geometry is null).
   * These are either isolated nodes,
   * or nodes which have edges from only a single Geometry incident on them.
   *
   * Isolated nodes are found because nodes in one graph which don't intersect
   * nodes in the other are not completely labelled by the initial process
   * of adding nodes to the nodeList.
   * To complete the labelling we need to check for nodes that lie in the
   * interior of edges, and in the interior of areas.
   * <p>
   * When each node labelling is completed, the labelling of the incident
   * edges is updated, to complete their labelling as well.
   */
  private void labelIncompleteNodes() {
    int nodeCount = 0;
    for (final Iterator ni = graph.getNodes().iterator(); ni.hasNext();) {
      final Node n = (Node)ni.next();
      final Label label = n.getLabel();
      if (n.isIsolated()) {
        nodeCount++;
        if (label.isNull(0)) {
          labelIncompleteNode(n, 0);
        } else {
          labelIncompleteNode(n, 1);
        }
      }
      // now update the labelling for the DirectedEdges incident on this node
      ((DirectedEdgeStar)n.getEdges()).updateLabelling(label);
      // n.print(System.out);
    }
    /*
     * int nPoly0 = arg[0].getGeometry().getNumGeometries(); int nPoly1 =
     * arg[1].getGeometry().getNumGeometries();
     * System.out.println("# isolated nodes= " + nodeCount + "   # poly[0] = " +
     * nPoly0 + "   # poly[1] = " + nPoly1);
     */
  }

  /**
   * For nodes which have edges from only one Geometry incident on them,
   * the previous step will have left their dirEdges with no labelling for the other
   * Geometry.  However, the sym dirEdge may have a labelling for the other
   * Geometry, so merge the two labels.
   */
  private void mergeSymLabels() {
    for (final Iterator nodeit = graph.getNodes().iterator(); nodeit.hasNext();) {
      final Node node = (Node)nodeit.next();
      ((DirectedEdgeStar)node.getEdges()).mergeSymLabels();
      // node.print(System.out);
    }
  }

  /**
   * If edges which have undergone dimensional collapse are found,
   * replace them with a new edge which is a L edge
   */
  private void replaceCollapsedEdges() {
    final List newEdges = new ArrayList();
    for (final Iterator it = edgeList.iterator(); it.hasNext();) {
      final Edge e = (Edge)it.next();
      if (e.isCollapsed()) {
        // Debug.print(e);
        it.remove();
        newEdges.add(e.getCollapsedEdge());
      }
    }
    edgeList.addAll(newEdges);
  }

  private void updateNodeLabelling() {
    // update the labels for nodes
    // The label for a node is updated from the edges incident on it
    // (Note that a node may have already been labelled
    // because it is a point in one of the input geometries)
    for (final Iterator nodeit = graph.getNodes().iterator(); nodeit.hasNext();) {
      final Node node = (Node)nodeit.next();
      final Label lbl = ((DirectedEdgeStar)node.getEdges()).getLabel();
      node.getLabel().merge(lbl);
    }
  }
}
