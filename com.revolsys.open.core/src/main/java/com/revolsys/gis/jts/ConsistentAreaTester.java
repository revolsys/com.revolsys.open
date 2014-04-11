package com.revolsys.gis.jts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.geometry.LineSegment;
import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geomgraph.Edge;
import com.revolsys.jts.geomgraph.GeometryGraph;
import com.revolsys.jts.operation.relate.EdgeEndBundle;
import com.revolsys.jts.operation.relate.RelateNode;
import com.revolsys.jts.operation.relate.RelateNodeGraph;

/**
 * Checks that a {@link GeometryGraph} representing an area
 * (a {@link Polygon} or {@link MultiPolygon} )
 * has consistent semantics for area geometries.
 * This check is required for any reasonable polygonal model
 * (including the OGC-SFS model, as well as models which allow ring self-intersection at single points)
 * <p>
 * Checks include:
 * <ul>
 * <li>test for rings which properly intersect
 * (but not for ring self-intersection, or intersections at vertices)
 * <li>test for consistent labelling at all node points
 * (this detects vertex intersections with invalid topology,
 * i.e. where the exterior side of an edge lies in the interior of the area)
 * <li>test for duplicate rings
 * </ul>
 * If an inconsistency is found the location of the problem
 * is recorded and is available to the caller.
 *
 * @version 1.7
 */
public class ConsistentAreaTester {

  private final LineIntersector li = new RobustLineIntersector();

  private final GeometryGraph geomGraph;

  private final RelateNodeGraph nodeGraph = new RelateNodeGraph();

  // the intersection point found (if any)
  private final List<Coordinates> invalidPoints = new ArrayList<Coordinates>();

  private final List<LineSegment> invalidLineSegments = new ArrayList<LineSegment>();

  /**
   * Creates a new tester for consistent areas.
   *
   * @param geomGraph the topology graph of the area geometry
   */
  public ConsistentAreaTester(final GeometryGraph geomGraph) {
    this.geomGraph = geomGraph;
  }

  public List<Coordinates> getInvalidPoints() {
    return invalidPoints;
  }

  /**
   * Checks for two duplicate rings in an area.
   * Duplicate rings are rings that are topologically equal
   * (that is, which have the same sequence of points up to point order).
   * If the area is topologically consistent (determined by calling the
   * <code>isNodeConsistentArea</code>,
   * duplicate rings can be found by checking for EdgeBundles which contain
   * more than one EdgeEnd.
   * (This is because topologically consistent areas cannot have two rings sharing
   * the same line segment, unless the rings are equal).
   * The start point of one of the equal rings will be placed in
   * invalidPoint.
   *
   * @return true if this area Geometry is topologically consistent but has two duplicate rings
   */
  public boolean hasDuplicateRings() {
    boolean hasDuplicate = false;
    for (final Iterator nodeIt = nodeGraph.getNodeIterator(); nodeIt.hasNext();) {
      final RelateNode node = (RelateNode)nodeIt.next();
      for (final Iterator i = node.getEdges().iterator(); i.hasNext();) {
        final EdgeEndBundle eeb = (EdgeEndBundle)i.next();
        if (eeb.getEdgeEnds().size() > 1) {
          final Edge edge = eeb.getEdge();
          final Coordinates point1 = CoordinatesUtil.get(edge.getCoordinate(0));
          final Coordinates point2 = CoordinatesUtil.get(edge.getCoordinate(1));
          invalidLineSegments.add(new LineSegment(point1, point2));
          hasDuplicate = true;
        }
      }
    }
    return hasDuplicate;
  }

  /**
   * Check all nodes to see if their labels are consistent with area topology.
   *
   * @return <code>true</code> if this area has a consistent node labelling
   */
  public boolean isNodeConsistentArea() {

    /**
     * To fully check validity, it is necessary to
     * compute ALL intersections, including self-intersections within a single edge.
     */
    final SegmentIntersector intersector = IsSimpleOp.computeIntersections(
      geomGraph, li, false);
    final List<Coordinates> properIntersections = intersector.getProperIntersections();
    if (properIntersections.isEmpty()) {
      nodeGraph.build(geomGraph);

      return isNodeEdgeAreaLabelsConsistent();
    } else {
      invalidPoints.addAll(properIntersections);
      return false;
    }

  }

  /**
   * Check all nodes to see if their labels are consistent.
   * If any are not, return false
   *
   * @return <code>true</code> if the edge area labels are consistent at this node
   */
  private boolean isNodeEdgeAreaLabelsConsistent() {
    boolean consistent = true;
    for (final Iterator nodeIt = nodeGraph.getNodeIterator(); nodeIt.hasNext();) {
      final RelateNode node = (RelateNode)nodeIt.next();
      if (!node.getEdges().isAreaLabelsConsistent(geomGraph)) {
        invalidPoints.add(CoordinatesUtil.get(node.getCoordinate()));
        consistent = false;
      }
    }
    return consistent;
  }

}
