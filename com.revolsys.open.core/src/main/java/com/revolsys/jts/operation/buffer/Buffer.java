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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.geom.TopologyException;
import com.revolsys.jts.geomgraph.Edge;
import com.revolsys.jts.geomgraph.EdgeList;
import com.revolsys.jts.geomgraph.Label;
import com.revolsys.jts.geomgraph.Node;
import com.revolsys.jts.geomgraph.PlanarGraph;
import com.revolsys.jts.geomgraph.Position;
import com.revolsys.jts.math.MathUtil;
import com.revolsys.jts.noding.IntersectionAdder;
import com.revolsys.jts.noding.MCIndexNoder;
import com.revolsys.jts.noding.Noder;
import com.revolsys.jts.noding.ScaledNoder;
import com.revolsys.jts.noding.SegmentString;
import com.revolsys.jts.noding.snapround.MCIndexSnapRounder;
import com.revolsys.jts.operation.overlay.OverlayNodeFactory;
import com.revolsys.jts.operation.overlay.PolygonBuilder;

//import debug.*;

/**
 * Computes the buffer of a geometry, for both positive and negative buffer distances.
 * <p>
 * In GIS, the positive (or negative) buffer of a geometry is defined as
 * the Minkowski sum (or difference) of the geometry
 * with a circle of radius equal to the absolute value of the buffer distance.
 * In the CAD/CAM world buffers are known as </i>offset curves</i>.
 * In morphological analysis the 
 * operation of postive and negative buffering 
 * is referred to as <i>erosion</i> and <i>dilation</i>
 * <p>
 * The buffer operation always returns a polygonal result.
 * The negative or zero-distance buffer of lines and points is always an empty {@link Polygon}.
 * <p>
 * Since true buffer curves may contain circular arcs,
 * computed buffer polygons can only be approximations to the true geometry.
 * The user can control the accuracy of the curve approximation by specifying
 * the number of linear segments used to approximate curves.
 * <p>
 * The <b>end cap style</b> of a linear buffer may be specified. The
 * following end cap styles are supported:
 * <ul
 * <li>{@link BufferParameters#CAP_ROUND} - the usual round end caps
 * <li>{@link BufferParameters#CAP_FLAT} - end caps are truncated flat at the line ends
 * <li>{@link BufferParameters#CAP_SQUARE} - end caps are squared off at the buffer distance beyond the line ends
 * </ul>
 * <p>
 *
 * @version 1.7
 */
public class Buffer {

  /**
   * A number of digits of precision which leaves some computational "headroom"
   * for floating point operations.
   * 
   * This value should be less than the decimal precision of double-precision values (16).
   */
  private static int MAX_PRECISION_DIGITS = 12;

  /**
   * Computes the buffer of a geometry for a given buffer distance.
   *
   * @param g the geometry to buffer
   * @param distance the buffer distance
   * @return the buffer of the input geometry
   */
  public static Geometry buffer(final Geometry geometry, final double distance) {
    final BufferParameters parameters = new BufferParameters();
    return buffer(geometry, distance, parameters);
  }

  /**
   * Comutes the buffer for a geometry for a given buffer distance
   * and accuracy of approximation.
   *
   * @param g the geometry to buffer
   * @param distance the buffer distance
   * @param parameters the buffer parameters to use
   * @return the buffer of the input geometry
   *
   */
  public static Geometry buffer(final Geometry geometry, final double distance,
    final BufferParameters parameters) {
    final PrecisionModel precisionModel = geometry.getPrecisionModel();
    try {
      final MCIndexNoder noder = new MCIndexNoder();
      final LineIntersector li = new RobustLineIntersector();
      li.setPrecisionModel(precisionModel);
      noder.setSegmentIntersector(new IntersectionAdder(li));
      return buffer(noder, precisionModel, geometry, distance, parameters);
    } catch (final RuntimeException e) {
      if (precisionModel.getType() == PrecisionModel.FIXED) {
        return bufferFixedPrecision(precisionModel, geometry, distance,
          parameters);
      } else {
        return bufferReducedPrecision(geometry, distance, parameters);
      }
    }
  }

  /**
   * Comutes the buffer for a geometry for a given buffer distance
   * and accuracy of approximation.
   *
   * @param g the geometry to buffer
   * @param distance the buffer distance
   * @param quadrantSegments the number of segments used to approximate a quarter circle
   * @return the buffer of the input geometry
   *
   */
  public static Geometry buffer(final Geometry geometry, final double distance,
    final int quadrantSegments) {
    final BufferParameters parameters = new BufferParameters();
    parameters.setQuadrantSegments(quadrantSegments);
    return buffer(geometry, distance, parameters);
  }

  /**
   * Comutes the buffer for a geometry for a given buffer distance
   * and accuracy of approximation.
   *
   * @param g the geometry to buffer
   * @param distance the buffer distance
   * @param quadrantSegments the number of segments used to approximate a quarter circle
   * @param endCapStyle the end cap style to use
   * @return the buffer of the input geometry
   *
   */
  public static Geometry buffer(final Geometry geometry, final double distance,
    final int quadrantSegments, final int endCapStyle) {
    final BufferParameters parameters = new BufferParameters();
    parameters.setQuadrantSegments(quadrantSegments);
    parameters.setEndCapStyle(endCapStyle);

    return buffer(geometry, distance, parameters);
  }

  private static Geometry buffer(final Noder noder,
    final PrecisionModel precisionModel, final Geometry geometry,
    final double distance, final BufferParameters parameters) {

    final GeometryFactory geometryFactory = geometry.getGeometryFactory();

    final OffsetCurveBuilder curveBuilder = new OffsetCurveBuilder(
      precisionModel, parameters);

    final OffsetCurveSetBuilder curveSetBuilder = new OffsetCurveSetBuilder(
      geometry, distance, curveBuilder);

    final List<SegmentString> bufferSegStrList = curveSetBuilder.getCurves();

    if (bufferSegStrList.size() <= 0) {
      return geometryFactory.polygon();
    } else {
      final EdgeList edgeList = new EdgeList();
      computeNodedEdges(noder, edgeList, bufferSegStrList);
      final PlanarGraph graph = new PlanarGraph(new OverlayNodeFactory());
      graph.addEdges(edgeList.getEdges());

      final List<BufferSubgraph> subgraphList = createSubgraphs(graph);
      final PolygonBuilder polyBuilder = new PolygonBuilder(geometryFactory);
      buildSubgraphs(subgraphList, polyBuilder);
      final List<Polygon> polygons = polyBuilder.getPolygons();

      if (polygons.size() <= 0) {
        return geometryFactory.polygon();
      } else {
        final Geometry resultGeom = geometryFactory.buildGeometry(polygons);
        return resultGeom;
      }
    }
  }

  private static Geometry bufferFixedPrecision(
    final PrecisionModel precisionModel, final Geometry geometry,
    final double distance, final BufferParameters parameters) {
    final PrecisionModel rounderPrecisionModel = new PrecisionModel(1.0);
    final MCIndexSnapRounder rounder = new MCIndexSnapRounder(
      rounderPrecisionModel);
    final double scale = precisionModel.getScale();
    final Noder noder = new ScaledNoder(rounder, scale);
    return Buffer.buffer(noder, precisionModel, geometry, distance, parameters);
  }

  private static Geometry bufferReducedPrecision(final Geometry geometry,
    final double distance, final BufferParameters parameters) {
    TopologyException saveException = null;
    // try and compute with decreasing precision
    for (int precDigits = MAX_PRECISION_DIGITS; precDigits >= 0; precDigits--) {
      try {
        final double sizeBasedScaleFactor = precisionScaleFactor(geometry,
          distance, precDigits);
        final PrecisionModel precisionModel = new PrecisionModel(
          sizeBasedScaleFactor);
        return bufferFixedPrecision(precisionModel, geometry, distance,
          parameters);
      } catch (final TopologyException e) {
        saveException = e;
      }
    }
    throw saveException;
  }

  /**
   * Completes the building of the input subgraphs by depth-labelling them,
   * and adds them to the PolygonBuilder.
   * The subgraph list must be sorted in rightmost-coordinate order.
   *
   * @param subgraphList the subgraphs to build
   * @param polyBuilder the PolygonBuilder which will build the final polygons
   */
  private static void buildSubgraphs(final List<BufferSubgraph> subgraphList,
    final PolygonBuilder polyBuilder) {
    final List<BufferSubgraph> processedGraphs = new ArrayList<>();
    for (final BufferSubgraph subgraph : subgraphList) {
      final Coordinates p = subgraph.getRightmostCoordinate();
      // int outsideDepth = 0;
      // if (polyBuilder.containsPoint(p))
      // outsideDepth = 1;
      final SubgraphDepthLocater locater = new SubgraphDepthLocater(
        processedGraphs);
      final int outsideDepth = locater.getDepth(p);
      // try {
      subgraph.computeDepth(outsideDepth);
      // }
      // catch (RuntimeException ex) {
      // // debugging only
      // //subgraph.saveDirEdges();
      // throw ex;
      // }
      subgraph.findResultEdges();
      processedGraphs.add(subgraph);
      polyBuilder.add(subgraph.getDirectedEdges(), subgraph.getNodes());
    }
  }

  private static void computeNodedEdges(final Noder noder,
    final EdgeList edgeList, final List<SegmentString> bufferSegStrList) {
    noder.computeNodes(bufferSegStrList);
    final Collection<SegmentString> nodedSegStrings = noder.getNodedSubstrings();
    // DEBUGGING ONLY
    // BufferDebug.saveEdges(nodedEdges, "run" + BufferDebug.runCount +
    // "_nodedEdges");

    for (final SegmentString segStr : nodedSegStrings) {
      /**
       * Discard edges which have zero length, 
       * since they carry no information and cause problems with topology building
       */
      final Coordinates[] pts = segStr.getCoordinates();
      if (pts.length == 2 && pts[0].equals2d(pts[1])) {
        continue;
      }

      final Label oldLabel = (Label)segStr.getData();
      final Edge edge = new Edge(segStr.getCoordinates(), new Label(oldLabel));
      insertUniqueEdge(edgeList, edge);
    }
    // saveEdges(edgeList.getEdges(), "run" + runCount + "_collapsedEdges");
  }

  private static List<BufferSubgraph> createSubgraphs(final PlanarGraph graph) {
    final List<BufferSubgraph> subgraphList = new ArrayList<>();
    for (final Node node : graph.getNodes()) {
      if (!node.isVisited()) {
        final BufferSubgraph subgraph = new BufferSubgraph();
        subgraph.create(node);
        subgraphList.add(subgraph);
      }
    }
    /**
     * Sort the subgraphs in descending order of their rightmost coordinate.
     * This ensures that when the Polygons for the subgraphs are built,
     * subgraphs for shells will have been built before the subgraphs for
     * any holes they contain.
     */
    Collections.sort(subgraphList, Collections.reverseOrder());
    return subgraphList;
  }

  /**
   * Compute the change in depth as an edge is crossed from R to L
   */
  private static int depthDelta(final Label label) {
    final Location lLoc = label.getLocation(0, Position.LEFT);
    final Location rLoc = label.getLocation(0, Position.RIGHT);
    if (lLoc == Location.INTERIOR && rLoc == Location.EXTERIOR) {
      return 1;
    } else if (lLoc == Location.EXTERIOR && rLoc == Location.INTERIOR) {
      return -1;
    }
    return 0;
  }

  /**
   * Inserted edges are checked to see if an identical edge already exists.
   * If so, the edge is not inserted, but its label is merged
   * with the existing edge.
   */
  private static void insertUniqueEdge(final EdgeList edgeList, final Edge edge) {
    // <FIX> MD 8 Oct 03 speed up identical edge lookup
    // fast lookup
    final Edge existingEdge = edgeList.findEqualEdge(edge);

    // If an identical edge already exists, simply update its label
    if (existingEdge != null) {
      final Label existingLabel = existingEdge.getLabel();

      Label labelToMerge = edge.getLabel();
      // check if new edge is in reverse direction to existing edge
      // if so, must flip the label before merging it
      if (!existingEdge.isPointwiseEqual(edge)) {
        labelToMerge = new Label(edge.getLabel());
        labelToMerge.flip();
      }
      existingLabel.merge(labelToMerge);

      // compute new depth delta of sum of edges
      final int mergeDelta = depthDelta(labelToMerge);
      final int existingDelta = existingEdge.getDepthDelta();
      final int newDelta = existingDelta + mergeDelta;
      existingEdge.setDepthDelta(newDelta);
    } else { // no matching existing edge was found
      // add this new edge to the list of edges in this graph
      // e.setName(name + edges.size());
      edgeList.add(edge);
      edge.setDepthDelta(depthDelta(edge.getLabel()));
    }
  }

  /**
   * Compute a scale factor to limit the precision of
   * a given combination of Geometry and buffer distance.
   * The scale factor is determined by
   * the number of digits of precision in the (geometry + buffer distance),
   * limited by the supplied <code>maxPrecisionDigits</code> value.
   * <p>
   * The scale factor is based on the absolute magnitude of the (geometry + buffer distance).
   * since this determines the number of digits of precision which must be handled.
   *
   * @param g the Geometry being buffered
   * @param distance the buffer distance
   * @param maxPrecisionDigits the max # of digits that should be allowed by
   *          the precision determined by the computed scale factor
   *
   * @return a scale factor for the buffer computation
   */
  private static double precisionScaleFactor(final Geometry g,
    final double distance, final int maxPrecisionDigits) {
    final BoundingBox env = g.getBoundingBox();
    final double envMax = MathUtil.max(Math.abs(env.getMaxX()),
      Math.abs(env.getMaxY()), Math.abs(env.getMinX()), Math.abs(env.getMinY()));

    final double expandByDistance = distance > 0.0 ? distance : 0.0;
    final double bufEnvMax = envMax + 2 * expandByDistance;

    // the smallest power of 10 greater than the buffer envelope
    final int bufEnvPrecisionDigits = (int)(Math.log(bufEnvMax) / Math.log(10) + 1.0);
    final int minUnitLog10 = maxPrecisionDigits - bufEnvPrecisionDigits;

    final double scaleFactor = Math.pow(10.0, minUnitLog10);
    return scaleFactor;
  }

}
