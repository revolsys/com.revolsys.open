package com.revolsys.gis.jts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.algorithm.LineIntersector;
import com.vividsolutions.jts.algorithm.MCPointInRing;
import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.RobustLineIntersector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geomgraph.Edge;
import com.vividsolutions.jts.geomgraph.EdgeIntersection;
import com.vividsolutions.jts.geomgraph.EdgeIntersectionList;
import com.vividsolutions.jts.geomgraph.GeometryGraph;
import com.vividsolutions.jts.operation.valid.ConnectedInteriorTester;
import com.vividsolutions.jts.operation.valid.IndexedNestedRingTester;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;
import com.vividsolutions.jts.util.Assert;

/**
 * Implements the algorithms required to compute the <code>isValid()</code> method
 * for {@link Geometry}s.
 * See the documentation for the various geometry types for a specification of validity.
 *
 * @version 1.7
 */
public class IsValidOp {
  /**
   * Find a point from the list of testCoords
   * that is NOT a node in the edge for the list of searchCoords
   *
   * @return the point found, or <code>null</code> if none found
   */
  public static Coordinate findPtNotNode(final Coordinate[] testCoords,
    final LinearRing searchRing, final GeometryGraph graph) {
    // find edge corresponding to searchRing.
    final Edge searchEdge = graph.findEdge(searchRing);
    // find a point in the testCoords which is not a node of the searchRing
    final EdgeIntersectionList eiList = searchEdge.getEdgeIntersectionList();
    // somewhat inefficient - is there a better way? (Use a node map, for
    // instance?)
    for (int i = 0; i < testCoords.length; i++) {
      final Coordinate pt = testCoords[i];
      if (!eiList.isIntersection(pt)) {
        return pt;
      }
    }
    return null;
  }

  /**
   * Checks whether a coordinate is valid for processing.
   * Coordinates are valid iff their x and y ordinates are in the
   * range of the floating point representation.
   *
   * @param coord the coordinate to validate
   * @return <code>true</code> if the coordinate is valid
   */
  public static boolean isValid(final Coordinate coord) {
    if (Double.isNaN(coord.x)) {
      return false;
    }
    if (Double.isInfinite(coord.x)) {
      return false;
    }
    if (Double.isNaN(coord.y)) {
      return false;
    }
    if (Double.isInfinite(coord.y)) {
      return false;
    }
    return true;
  }

  /**
   * Tests whether a {@link Geometry} is valid.
   * @param geom the Geometry to test
   * @return true if the geometry is valid
   */
  public static boolean isValid(final Geometry geom) {
    final IsValidOp isValidOp = new IsValidOp(geom);
    return isValidOp.isValid();
  }

  private final Geometry parentGeometry; // the base Geometry to be validated

  /**
   * If the following condition is TRUE JTS will validate inverted shells and exverted holes
   * (the ESRI SDE model)
   */
  private boolean isSelfTouchingRingFormingHoleValid = false;

  private final List<TopologyValidationError> errors = new ArrayList<TopologyValidationError>();

  public IsValidOp(final Geometry parentGeometry) {
    this.parentGeometry = parentGeometry;
  }

  private boolean addError(final int errorType, final Coordinate point) {
    return addError(new TopologyValidationError(errorType, point));
  }

  public boolean addError(final int errorType, final Coordinates point) {
    return addError(errorType, new Coordinate(point.getX(), point.getY()));
  }

  private boolean addError(final TopologyValidationError error) {
    this.errors.add(error);
    return false;
  }

  private boolean checkClosedRing(final LinearRing ring) {
    if (ring.isClosed()) {
      return true;
    } else {
      Coordinate pt = null;
      if (ring.getNumPoints() >= 1) {
        pt = ring.getCoordinateN(0);
      }
      addError(TopologyValidationError.RING_NOT_CLOSED, pt);
      return false;
    }
  }

  private boolean checkClosedRings(final Polygon poly) {
    boolean valid = true;
    valid &= checkClosedRing((LinearRing)poly.getExteriorRing());
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      valid &= checkClosedRing((LinearRing)poly.getInteriorRingN(i));

    }
    return valid;
  }

  private boolean checkConnectedInteriors(final GeometryGraph graph) {
    final ConnectedInteriorTester cit = new ConnectedInteriorTester(graph);
    if (cit.isInteriorsConnected()) {
      return true;
    } else {
      return addError(TopologyValidationError.DISCONNECTED_INTERIOR,
        cit.getCoordinate());
    }
  }

  /**
   * Checks that the arrangement of edges in a polygonal geometry graph
   * forms a consistent area.
   *
   * @param graph
   *
   * @see ConsistentAreaTester
   */
  private boolean checkConsistentArea(final GeometryGraph graph) {
    final ConsistentAreaTester cat = new ConsistentAreaTester(graph);
    final boolean isValidArea = cat.isNodeConsistentArea();
    if (!isValidArea) {
      for (final Coordinates point : cat.getInvalidPoints()) {
        addError(TopologyValidationError.SELF_INTERSECTION, point);
      }
      return false;
    } else if (cat.hasDuplicateRings()) {
      for (final Coordinates point : cat.getInvalidPoints()) {
        addError(TopologyValidationError.DUPLICATE_RINGS, point);
      }

    }
    return true;
  }

  /**
   * Tests that each hole is inside the polygon shell.
   * This routine assumes that the holes have previously been tested
   * to ensure that all vertices lie on the shell oon the same side of it
   * (i.e that the hole rings do not cross the shell ring).
   * In other words, this test is only correct if the ConsistentArea test is passed first.
   * Given this, a simple point-in-polygon test of a single point in the hole can be used,
   * provided the point is chosen such that it does not lie on the shell.
   *
   * @param p the polygon to be tested for hole inclusion
   * @param graph a GeometryGraph incorporating the polygon
   */
  private boolean checkHolesInShell(final Polygon p, final GeometryGraph graph) {
    boolean valid = true;
    final LinearRing shell = (LinearRing)p.getExteriorRing();

    // PointInRing pir = new SimplePointInRing(shell);
    // PointInRing pir = new SIRtreePointInRing(shell);
    final PointInRing pir = new MCPointInRing(shell);

    for (int i = 0; i < p.getNumInteriorRing(); i++) {

      final LinearRing hole = (LinearRing)p.getInteriorRingN(i);
      final Coordinate holePt = findPtNotNode(hole.getCoordinates(), shell,
        graph);
      /**
       * If no non-node hole vertex can be found, the hole must
       * split the polygon into disconnected interiors.
       * This will be caught by a subsequent check.
       */
      if (holePt == null) {
        return valid;
      }

      final boolean outside = !pir.isInside(holePt);
      if (outside) {
        valid = addError(TopologyValidationError.HOLE_OUTSIDE_SHELL, holePt);
      }
    }
    return valid;
  }

  /**
   * Tests that no hole is nested inside another hole.
   * This routine assumes that the holes are disjoint.
   * To ensure this, holes have previously been tested
   * to ensure that:
   * <ul>
   * <li>they do not partially overlap
   *      (checked by <code>checkRelateConsistency</code>)
   * <li>they are not identical
   *      (checked by <code>checkRelateConsistency</code>)
   * </ul>
   */
  private boolean checkHolesNotNested(final Polygon p, final GeometryGraph graph) {
    final IndexedNestedRingTester nestedTester = new IndexedNestedRingTester(
      graph);
    // SimpleNestedRingTester nestedTester = new SimpleNestedRingTester(arg[0]);
    // SweeplineNestedRingTester nestedTester = new
    // SweeplineNestedRingTester(arg[0]);

    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      final LinearRing innerHole = (LinearRing)p.getInteriorRingN(i);
      nestedTester.add(innerHole);
    }
    final boolean isNonNested = nestedTester.isNonNested();
    if (isNonNested) {
      return true;
    } else {
      return addError(TopologyValidationError.NESTED_HOLES,
        nestedTester.getNestedPoint());
    }
  }

  private boolean checkInvalidCoordinates(final Coordinate[] coords) {
    boolean valid = true;
    for (int i = 0; i < coords.length; i++) {
      if (!isValid(coords[i])) {
        valid = addError(TopologyValidationError.INVALID_COORDINATE, coords[i]);
      }
    }
    return valid;
  }

  private boolean checkInvalidCoordinates(final Polygon poly) {
    boolean valid = checkInvalidCoordinates(poly.getExteriorRing()
      .getCoordinates());
    for (int i = 0; i < poly.getNumInteriorRing(); i++) {
      valid &= checkInvalidCoordinates(poly.getInteriorRingN(i)
        .getCoordinates());
    }
    return valid;
  }

  /**
   * Check that a ring does not self-intersect, except at its endpoints.
   * Algorithm is to count the number of times each node along edge occurs.
   * If any occur more than once, that must be a self-intersection.
   */
  private boolean checkNoSelfIntersectingRing(final EdgeIntersectionList eiList) {
    boolean valid = true;
    final Set<Coordinate> nodeSet = new TreeSet<Coordinate>();
    boolean isFirst = true;
    for (final Iterator i = eiList.iterator(); i.hasNext();) {
      final EdgeIntersection ei = (EdgeIntersection)i.next();
      if (isFirst) {
        isFirst = false;
        continue;
      }
      if (nodeSet.contains(ei.coord)) {
        valid = addError(TopologyValidationError.RING_SELF_INTERSECTION,
          ei.coord);
      } else {
        nodeSet.add(ei.coord);
      }
    }
    return valid;
  }

  /**
   * Check that there is no ring which self-intersects (except of course at its endpoints).
   * This is required by OGC topology rules (but not by other models
   * such as ESRI SDE, which allow inverted shells and exverted holes).
   *
   * @param graph the topology graph of the geometry
   */
  private boolean checkNoSelfIntersectingRings(final GeometryGraph graph) {
    boolean valid = true;
    for (final Iterator i = graph.getEdgeIterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      valid &= checkNoSelfIntersectingRing(e.getEdgeIntersectionList());
    }
    return valid;
  }

  /**
   * This routine checks to see if a shell is properly contained in a hole.
   * It assumes that the edges of the shell and hole do not
   * properly intersect.
   *
   * @return <code>null</code> if the shell is properly contained, or
   *   a Coordinate which is not inside the hole if it is not
   *
   */
  private Coordinate checkShellInsideHole(final LinearRing shell,
    final LinearRing hole, final GeometryGraph graph) {
    final Coordinate[] shellPts = shell.getCoordinates();
    final Coordinate[] holePts = hole.getCoordinates();
    // TODO: improve performance of this - by sorting pointlists for instance?
    final Coordinate shellPt = findPtNotNode(shellPts, hole, graph);
    // if point is on shell but not hole, check that the shell is inside the
    // hole
    if (shellPt != null) {
      final boolean insideHole = CGAlgorithms.isPointInRing(shellPt, holePts);
      if (!insideHole) {
        return shellPt;
      }
    }
    final Coordinate holePt = findPtNotNode(holePts, shell, graph);
    // if point is on hole but not shell, check that the hole is outside the
    // shell
    if (holePt != null) {
      final boolean insideShell = CGAlgorithms.isPointInRing(holePt, shellPts);
      if (insideShell) {
        return holePt;
      }
      return null;
    }
    Assert.shouldNeverReachHere("points in shell and hole appear to be equal");
    return null;
  }

  /**
   * Check if a shell is incorrectly nested within a polygon.  This is the case
   * if the shell is inside the polygon shell, but not inside a polygon hole.
   * (If the shell is inside a polygon hole, the nesting is valid.)
   * <p>
   * The algorithm used relies on the fact that the rings must be properly contained.
   * E.g. they cannot partially overlap (this has been previously checked by
   * <code>checkRelateConsistency</code> )
   */
  private boolean checkShellNotNested(final LinearRing shell, final Polygon p,
    final GeometryGraph graph) {
    boolean valid = true;
    final Coordinate[] shellPts = shell.getCoordinates();
    // test if shell is inside polygon shell
    final LinearRing polyShell = (LinearRing)p.getExteriorRing();
    final Coordinate[] polyPts = polyShell.getCoordinates();
    final Coordinate shellPt = findPtNotNode(shellPts, polyShell, graph);
    // if no point could be found, we can assume that the shell is outside the
    // polygon
    if (shellPt != null) {
      final boolean insidePolyShell = CGAlgorithms.isPointInRing(shellPt,
        polyPts);
      if (insidePolyShell) {

        // if no holes, this is an error!
        if (p.getNumInteriorRing() <= 0) {
          valid = addError(TopologyValidationError.NESTED_SHELLS, shellPt);
        } else {

          /**
           * Check if the shell is inside one of the holes.
           * This is the case if one of the calls to checkShellInsideHole
           * returns a null coordinate.
           * Otherwise, the shell is not properly contained in a hole, which is an error.
           */
          Coordinate badNestedPt = null;
          for (int i = 0; i < p.getNumInteriorRing(); i++) {
            final LinearRing hole = (LinearRing)p.getInteriorRingN(i);
            badNestedPt = checkShellInsideHole(shell, hole, graph);
            if (badNestedPt == null) {
              return valid;
            }
          }

          valid &= addError(TopologyValidationError.NESTED_SHELLS, badNestedPt);
        }
      }
    }
    return valid;
  }

  /**
   * Tests that no element polygon is wholly in the interior of another element polygon.
   * <p>
   * Preconditions:
   * <ul>
   * <li>shells do not partially overlap
   * <li>shells do not touch along an edge
   * <li>no duplicate rings exist
   * </ul>
   * This routine relies on the fact that while polygon shells may touch at one or
   * more vertices, they cannot touch at ALL vertices.
   */
  private boolean checkShellsNotNested(final MultiPolygon multiPolygon,
    final GeometryGraph graph) {
    boolean valid = true;
    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
      final Polygon p = (Polygon)multiPolygon.getGeometryN(i);
      final LinearRing shell = (LinearRing)p.getExteriorRing();
      for (int j = 0; j < multiPolygon.getNumGeometries(); j++) {
        if (i == j) {
          continue;
        }
        final Polygon p2 = (Polygon)multiPolygon.getGeometryN(j);
        valid &= checkShellNotNested(shell, p2, graph);
      }
    }
    return valid;
  }

  private boolean checkTooFewPoints(final GeometryGraph graph) {
    if (graph.hasTooFewPoints()) {
      return addError(TopologyValidationError.TOO_FEW_POINTS,
        graph.getInvalidPoint());
    } else {
      return true;
    }
  }

  private boolean checkValid(final Geometry g) {
    errors.clear();

    // empty geometries are always valid!
    if (g.isEmpty()) {
      return true;
    } else if (g instanceof Point) {
      return checkValid((Point)g);
    } else if (g instanceof MultiPoint) {
      return checkValid((MultiPoint)g);
    } else if (g instanceof LinearRing) {
      return checkValid((LinearRing)g);
    } else if (g instanceof LineString) {
      return checkValid((LineString)g);
    } else if (g instanceof Polygon) {
      return checkValid((Polygon)g);
    } else if (g instanceof MultiPolygon) {
      return checkValid((MultiPolygon)g);
    } else if (g instanceof GeometryCollection) {
      return checkValid((GeometryCollection)g);
    } else {
      throw new UnsupportedOperationException(g.getClass().getName());
    }
  }

  private boolean checkValid(final GeometryCollection geometryCollection) {
    boolean valid = true;
    for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
      final Geometry geometry = geometryCollection.getGeometryN(i);
      valid &= checkValid(geometry);
    }
    return valid;
  }

  /**
   * Checks validity of a LinearRing.
   */
  private boolean checkValid(final LinearRing g) {
    boolean valid = checkInvalidCoordinates(g.getCoordinates());
    valid &= checkClosedRing(g);

    final GeometryGraph graph = new GeometryGraph(0, g);
    valid &= checkTooFewPoints(graph);
    final LineIntersector li = new RobustLineIntersector();
    final SegmentIntersector segmentIntersector = IsSimpleOp.computeIntersections(
      graph, li, true);
    final List<Coordinates> properIntersections = segmentIntersector.getProperIntersections();
    if (properIntersections.isEmpty()) {
      valid &= checkNoSelfIntersectingRings(graph);
    } else {
      for (final Coordinates point : properIntersections) {
        addError(TopologyValidationError.RING_SELF_INTERSECTION, point);
      }
      valid = false;
    }
    return valid;
  }

  /**
   * Checks validity of a LineString.  Almost anything goes for linestrings!
   */
  private boolean checkValid(final LineString g) {
    boolean valid = checkInvalidCoordinates(g.getCoordinates());
    final GeometryGraph graph = new GeometryGraph(0, g);
    valid &= checkTooFewPoints(graph);
    return valid;
  }

  /**
   * Checks validity of a MultiPoint.
   */
  private boolean checkValid(final MultiPoint g) {
    return checkInvalidCoordinates(g.getCoordinates());
  }

  private boolean checkValid(final MultiPolygon multiPolygon) {
    boolean valid = true;
    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
      final Polygon p = (Polygon)multiPolygon.getGeometryN(i);
      valid &= checkInvalidCoordinates(p);
      valid &= checkClosedRings(p);
    }
    if (valid) {

      final GeometryGraph graph = new GeometryGraph(0, multiPolygon);

      if (checkTooFewPoints(graph)) {
        if (checkConsistentArea(graph)) {
          if (!isSelfTouchingRingFormingHoleValid) {
            valid &= checkNoSelfIntersectingRings(graph);
          }
          for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            final Polygon p = (Polygon)multiPolygon.getGeometryN(i);
            valid &= checkHolesInShell(p, graph);
          }
          for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {
            final Polygon p = (Polygon)multiPolygon.getGeometryN(i);
            valid &= checkHolesNotNested(p, graph);
          }
          valid &= checkShellsNotNested(multiPolygon, graph);
          valid &= checkConnectedInteriors(graph);
        }
      } else {
        valid = false;
      }
    }
    return valid;
  }

  /**
   * Checks validity of a Point.
   */
  private boolean checkValid(final Point g) {
    return checkInvalidCoordinates(g.getCoordinates());
  }

  /**
   * Checks the validity of a polygon.
   * Sets the validErr flag.
   */
  private boolean checkValid(final Polygon g) {
    if (checkInvalidCoordinates(g)) {
      if (checkClosedRings(g)) {

        final GeometryGraph graph = new GeometryGraph(0, g);

        if (checkTooFewPoints(graph)) {
          if (checkConsistentArea(graph)) {

            boolean valid = true;
            if (!isSelfTouchingRingFormingHoleValid) {
              valid &= checkNoSelfIntersectingRings(graph);
            }
            valid &= checkHolesInShell(g, graph);
            // SLOWcheckHolesNotNested(g);
            valid &= checkHolesNotNested(g, graph);
            valid &= checkConnectedInteriors(graph);
            return valid;
          }
        }
      }
    }
    return false;
  }

  public List<TopologyValidationError> getErrors() {
    return errors;
  }

  public boolean isValid() {
    checkValid(parentGeometry);
    return errors.isEmpty();
  }

  /**
   * Sets whether polygons using <b>Self-Touching Rings</b> to form
   * holes are reported as valid.
   * If this flag is set, the following Self-Touching conditions
   * are treated as being valid:
   * <ul>
   * <li>the shell ring self-touches to create a hole touching the shell
   * <li>a hole ring self-touches to create two holes touching at a point
   * </ul>
   * <p>
   * The default (following the OGC SFS standard)
   * is that this condition is <b>not</b> valid (<code>false</code>).
   * <p>
   * This does not affect whether Self-Touching Rings
   * disconnecting the polygon interior are considered valid
   * (these are considered to be <b>invalid</b> under the SFS, and many other
   * spatial models as well).
   * This includes "bow-tie" shells,
   * which self-touch at a single point causing the interior to
   * be disconnected,
   * and "C-shaped" holes which self-touch at a single point causing an island to be formed.
   *
   * @param isValid states whether geometry with this condition is valid
   */
  public void setSelfTouchingRingFormingHoleValid(final boolean isValid) {
    isSelfTouchingRingFormingHoleValid = isValid;
  }

}
