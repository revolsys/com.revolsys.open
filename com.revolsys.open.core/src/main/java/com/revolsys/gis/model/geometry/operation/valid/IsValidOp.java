package com.revolsys.gis.model.geometry.operation.valid;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiPoint;
import com.revolsys.gis.model.geometry.MultiPolygon;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.operation.algorithm.MCPointInRing;
import com.revolsys.gis.model.geometry.operation.algorithm.PointInRing;
import com.revolsys.gis.model.geometry.operation.geomgraph.Edge;
import com.revolsys.gis.model.geometry.operation.geomgraph.EdgeIntersection;
import com.revolsys.gis.model.geometry.operation.geomgraph.EdgeIntersectionList;
import com.revolsys.gis.model.geometry.operation.geomgraph.GeometryGraph;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.RobustLineIntersector;
import com.revolsys.jts.geom.CoordinatesList;
import com.revolsys.jts.util.Assert;

/**
 * Implements the algorithms required to compute the <code>isValid()</code>
 * method for {@link Geometry}s. See the documentation for the various geometry
 * types for a specification of validity.
 * 
 * @version 1.7
 */
public class IsValidOp {
  /**
   * Find a point from the list of testCoords that is NOT a node in the edge for
   * the list of searchCoords
   * 
   * @return the point found, or <code>null</code> if none found
   */
  public static Coordinates findPtNotNode(final CoordinatesList testCoords,
    final LinearRing searchRing, final GeometryGraph graph) {
    // find edge corresponding to searchRing.
    final Edge searchEdge = graph.findEdge(searchRing);
    // find a point in the testCoords which is not a node of the searchRing
    final EdgeIntersectionList eiList = searchEdge.getEdgeIntersectionList();
    // somewhat inefficient - is there a better way? (Use a node map, for
    // instance?)
    for (int i = 0; i < testCoords.size(); i++) {
      final Coordinates pt = testCoords.get(i);
      if (!eiList.isIntersection(pt)) {
        return pt;
      }
    }
    return null;
  }

  /**
   * Checks whether a coordinate is valid for processing. Coordinates are valid
   * iff their x and y ordinates are in the range of the floating point
   * representation.
   * 
   * @param coord the coordinate to validate
   * @return <code>true</code> if the coordinate is valid
   */
  public static boolean isValid(final Coordinates coord) {
    if (Double.isNaN(coord.getX())) {
      return false;
    }
    if (Double.isInfinite(coord.getX())) {
      return false;
    }
    if (Double.isNaN(coord.getY())) {
      return false;
    }
    if (Double.isInfinite(coord.getY())) {
      return false;
    }
    return true;
  }

  /**
   * Tests whether a {@link Geometry} is valid.
   * 
   * @param geom the Geometry to test
   * @return true if the geometry is valid
   */
  public static boolean isValid(final Geometry geom) {
    final IsValidOp isValidOp = new IsValidOp(geom);
    return isValidOp.isValid();
  }

  private final Geometry parentGeometry; // the base Geometry to be validated

  /**
   * If the following condition is TRUE JTS will validate inverted shells and
   * exverted holes (the ESRI SDE model)
   */
  private boolean isSelfTouchingRingFormingHoleValid = false;

  private TopologyValidationError validErr;

  public IsValidOp(final Geometry parentGeometry) {
    this.parentGeometry = parentGeometry;
  }

  private void checkClosedRing(final LinearRing ring) {
    if (!ring.isClosed()) {
      Coordinates pt = null;
      if (ring.size() >= 1) {
        pt = ring.get(0);
      }
      validErr = new TopologyValidationError(
        TopologyValidationError.RING_NOT_CLOSED, pt);
    }
  }

  private void checkClosedRings(final Polygon poly) {
    checkClosedRing(poly.getExteriorRing());
    if (validErr != null) {
      return;
    }
    for (int i = 1; i < poly.getRingCount(); i++) {
      checkClosedRing(poly.getRing(i));
      if (validErr != null) {
        return;
      }
    }
  }

  private void checkConnectedInteriors(final GeometryGraph graph) {
    final ConnectedInteriorTester cit = new ConnectedInteriorTester(graph);
    if (!cit.isInteriorsConnected()) {
      validErr = new TopologyValidationError(
        TopologyValidationError.DISCONNECTED_INTERIOR, cit.getCoordinate());
    }
  }

  /**
   * Checks that the arrangement of edges in a polygonal geometry graph forms a
   * consistent area.
   * 
   * @param graph
   * @see ConsistentAreaTester
   */
  private void checkConsistentArea(final GeometryGraph graph) {
    final ConsistentAreaTester cat = new ConsistentAreaTester(graph);
    final boolean isValidArea = cat.isNodeConsistentArea();
    if (!isValidArea) {
      validErr = new TopologyValidationError(
        TopologyValidationError.SELF_INTERSECTION, cat.getInvalidPoint());
      return;
    }
    if (cat.hasDuplicateRings()) {
      validErr = new TopologyValidationError(
        TopologyValidationError.DUPLICATE_RINGS, cat.getInvalidPoint());
    }
  }

  /**
   * Tests that each hole is inside the polygon shell. This routine assumes that
   * the holes have previously been tested to ensure that all vertices lie on
   * the shell oon the same side of it (i.e that the hole rings do not cross the
   * shell ring). In other words, this test is only correct if the
   * ConsistentArea test is passed first. Given this, a simple point-in-polygon
   * test of a single point in the hole can be used, provided the point is
   * chosen such that it does not lie on the shell.
   * 
   * @param p the polygon to be tested for hole inclusion
   * @param graph a GeometryGraph incorporating the polygon
   */
  private void checkHolesInShell(final Polygon p, final GeometryGraph graph) {
    final LinearRing shell = p.getExteriorRing();

    // PointInRing pir = new SimplePointInRing(shell);
    // PointInRing pir = new SIRtreePointInRing(shell);
    final PointInRing pir = new MCPointInRing(shell);

    for (int i = 1; i < p.getRingCount(); i++) {

      final LinearRing hole = p.getRing(i);
      final Coordinates holePt = findPtNotNode(hole, shell, graph);
      /**
       * If no non-node hole vertex can be found, the hole must split the
       * polygon into disconnected interiors. This will be caught by a
       * subsequent check.
       */
      if (holePt == null) {
        return;
      }

      final boolean outside = !pir.isInside(holePt);
      if (outside) {
        validErr = new TopologyValidationError(
          TopologyValidationError.HOLE_OUTSIDE_SHELL, holePt);
        return;
      }
    }
  }

  /**
   * Tests that no hole is nested inside another hole. This routine assumes that
   * the holes are disjoint. To ensure this, holes have previously been tested
   * to ensure that:
   * <ul>
   * <li>they do not partially overlap (checked by
   * <code>checkRelateConsistency</code>)
   * <li>they are not identical (checked by <code>checkRelateConsistency</code>)
   * </ul>
   */
  private void checkHolesNotNested(final Polygon p, final GeometryGraph graph) {
    final IndexedNestedRingTester nestedTester = new IndexedNestedRingTester(
      graph);
    // SimpleNestedRingTester nestedTester = new SimpleNestedRingTester(arg[0]);
    // SweeplineNestedRingTester nestedTester = new
    // SweeplineNestedRingTester(arg[0]);

    for (int i = 1; i < p.getRingCount(); i++) {
      final LinearRing innerHole = p.getRing(i);
      nestedTester.add(innerHole);
    }
    final boolean isNonNested = nestedTester.isNonNested();
    if (!isNonNested) {
      validErr = new TopologyValidationError(
        TopologyValidationError.NESTED_HOLES, nestedTester.getNestedPoint());
    }
  }

  private boolean checkInvalidCoordinates(final Coordinates point) {
    if (isValid(point)) {
      return true;
    } else {
      validErr = new TopologyValidationError(
        TopologyValidationError.INVALID_COORDINATE, point);
      return false;
    }
  }

  private boolean checkInvalidCoordinates(final CoordinatesList coords) {
    for (int i = 0; i < coords.size(); i++) {
      final Coordinates point = coords.get(i);
      if (!checkInvalidCoordinates(point)) {
        return false;
      }
    }
    return true;
  }

  private boolean checkInvalidCoordinates(final List<CoordinatesList> coords) {
    for (final CoordinatesList points : coords) {
      if (!checkInvalidCoordinates(points)) {
        return false;
      }
    }
    return true;
  }

  private void checkInvalidCoordinates(final Polygon poly) {
    checkInvalidCoordinates(poly.getExteriorRing());
    if (validErr != null) {
      return;
    }
    for (int i = 1; i < poly.getRingCount(); i++) {
      checkInvalidCoordinates(poly.getRing(i));
      if (validErr != null) {
        return;
      }
    }
  }

  /**
   * Check that a ring does not self-intersect, except at its endpoints.
   * Algorithm is to count the number of times each node along edge occurs. If
   * any occur more than once, that must be a self-intersection.
   */
  private void checkNoSelfIntersectingRing(final EdgeIntersectionList eiList) {
    final Set nodeSet = new TreeSet();
    boolean isFirst = true;
    for (final Iterator i = eiList.iterator(); i.hasNext();) {
      final EdgeIntersection ei = (EdgeIntersection)i.next();
      if (isFirst) {
        isFirst = false;
        continue;
      }
      if (nodeSet.contains(ei.coord)) {
        validErr = new TopologyValidationError(
          TopologyValidationError.RING_SELF_INTERSECTION, ei.coord);
        return;
      } else {
        nodeSet.add(ei.coord);
      }
    }
  }

  /**
   * Check that there is no ring which self-intersects (except of course at its
   * endpoints). This is required by OGC topology rules (but not by other models
   * such as ESRI SDE, which allow inverted shells and exverted holes).
   * 
   * @param graph the topology graph of the geometry
   */
  private void checkNoSelfIntersectingRings(final GeometryGraph graph) {
    for (final Iterator i = graph.getEdgeIterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      checkNoSelfIntersectingRing(e.getEdgeIntersectionList());
      if (validErr != null) {
        return;
      }
    }
  }

  /**
   * This routine checks to see if a shell is properly contained in a hole. It
   * assumes that the edges of the shell and hole do not properly intersect.
   * 
   * @return <code>null</code> if the shell is properly contained, or a
   *         Coordinates which is not inside the hole if it is not
   */
  private Coordinates checkShellInsideHole(final LinearRing shell,
    final LinearRing hole, final GeometryGraph graph) {
    final CoordinatesList shellPts = shell;
    final CoordinatesList holePts = hole;
    // TODO: improve performance of this - by sorting pointlists for instance?
    final Coordinates shellPt = findPtNotNode(shellPts, hole, graph);
    // if point is on shell but not hole, check that the shell is inside the
    // hole
    if (shellPt != null) {
      final boolean insideHole = CoordinatesListUtil.isPointInRing(shellPt,
        holePts);
      if (!insideHole) {
        return shellPt;
      }
    }
    final Coordinates holePt = findPtNotNode(holePts, shell, graph);
    // if point is on hole but not shell, check that the hole is outside the
    // shell
    if (holePt != null) {
      final boolean insideShell = CoordinatesListUtil.isPointInRing(holePt,
        shellPts);
      if (insideShell) {
        return holePt;
      }
      return null;
    }
    Assert.shouldNeverReachHere("points in shell and hole appear to be equal");
    return null;
  }

  /**
   * Check if a shell is incorrectly nested within a polygon. This is the case
   * if the shell is inside the polygon shell, but not inside a polygon hole.
   * (If the shell is inside a polygon hole, the nesting is valid.)
   * <p>
   * The algorithm used relies on the fact that the rings must be properly
   * contained. E.g. they cannot partially overlap (this has been previously
   * checked by <code>checkRelateConsistency</code> )
   */
  private void checkShellNotNested(final LinearRing shell, final Polygon p,
    final GeometryGraph graph) {
    final CoordinatesList shellPts = shell;
    // test if shell is inside polygon shell
    final LinearRing polyShell = p.getExteriorRing();
    final CoordinatesList polyPts = polyShell;
    final Coordinates shellPt = findPtNotNode(shellPts, polyShell, graph);
    // if no point could be found, we can assume that the shell is outside the
    // polygon
    if (shellPt == null) {
      return;
    }
    final boolean insidePolyShell = CoordinatesListUtil.isPointInRing(shellPt,
      polyPts);
    if (!insidePolyShell) {
      return;
    }

    // if no holes, this is an error!
    if (p.getRingCount() <= 0) {
      validErr = new TopologyValidationError(
        TopologyValidationError.NESTED_SHELLS, shellPt);
      return;
    }

    /**
     * Check if the shell is inside one of the holes. This is the case if one of
     * the calls to checkShellInsideHole returns a null coordinate. Otherwise,
     * the shell is not properly contained in a hole, which is an error.
     */
    Coordinates badNestedPt = null;
    for (int i = 1; i < p.getRingCount(); i++) {
      final LinearRing hole = p.getRing(i);
      badNestedPt = checkShellInsideHole(shell, hole, graph);
      if (badNestedPt == null) {
        return;
      }
    }
    validErr = new TopologyValidationError(
      TopologyValidationError.NESTED_SHELLS, badNestedPt);
  }

  /**
   * Tests that no element polygon is wholly in the interior of another element
   * polygon.
   * <p>
   * Preconditions:
   * <ul>
   * <li>shells do not partially overlap
   * <li>shells do not touch along an edge
   * <li>no duplicate rings exist
   * </ul>
   * This routine relies on the fact that while polygon shells may touch at one
   * or more vertices, they cannot touch at ALL vertices.
   */
  private void checkShellsNotNested(final MultiPolygon mp,
    final GeometryGraph graph) {
    for (int i = 0; i < mp.getGeometryCount(); i++) {
      final Polygon p = (Polygon)mp.getGeometry(i);
      final LinearRing shell = p.getExteriorRing();
      for (int j = 0; j < mp.getGeometryCount(); j++) {
        if (i == j) {
          continue;
        }
        final Polygon p2 = (Polygon)mp.getGeometry(j);
        checkShellNotNested(shell, p2, graph);
        if (validErr != null) {
          return;
        }
      }
    }
  }

  private void checkTooFewPoints(final GeometryGraph graph) {
    if (graph.hasTooFewPoints()) {
      validErr = new TopologyValidationError(
        TopologyValidationError.TOO_FEW_POINTS, graph.getInvalidPoint());
      return;
    }
  }

  private void checkValid(final Geometry g) {
    validErr = null;

    // empty geometries are always valid!
    if (g.isEmpty()) {
      return;
    }

    if (g instanceof Point) {
      checkValid((Point)g);
    } else if (g instanceof MultiPoint) {
      checkValid((MultiPoint)g);
    } else if (g instanceof LinearRing) {
      checkValid((LinearRing)g);
    } else if (g instanceof LineString) {
      checkValid((LineString)g);
    } else if (g instanceof Polygon) {
      checkValid((Polygon)g);
    } else if (g instanceof MultiPolygon) {
      checkValid((MultiPolygon)g);
    } else if (g instanceof GeometryCollection) {
      checkValid((GeometryCollection)g);
    } else {
      throw new UnsupportedOperationException(g.getClass().getName());
    }
  }

  private void checkValid(final GeometryCollection gc) {
    for (int i = 0; i < gc.getGeometryCount(); i++) {
      final Geometry g = gc.getGeometry(i);
      checkValid(g);
      if (validErr != null) {
        return;
      }
    }
  }

  /**
   * Checks validity of a LinearRing.
   */
  private void checkValid(final LinearRing g) {
    checkInvalidCoordinates(g);
    if (validErr != null) {
      return;
    }
    checkClosedRing(g);
    if (validErr != null) {
      return;
    }

    final GeometryGraph graph = new GeometryGraph(0, g);
    checkTooFewPoints(graph);
    if (validErr != null) {
      return;
    }
    final LineIntersector li = new RobustLineIntersector();
    graph.computeSelfNodes(li, true);
    checkNoSelfIntersectingRings(graph);
  }

  /**
   * Checks validity of a LineString. Almost anything goes for linestrings!
   */
  private void checkValid(final LineString g) {
    checkInvalidCoordinates(g);
    if (validErr != null) {
      return;
    }
    final GeometryGraph graph = new GeometryGraph(0, g);
    checkTooFewPoints(graph);
  }

  /**
   * Checks validity of a MultiPoint.
   */
  private void checkValid(final MultiPoint g) {
    checkInvalidCoordinates(g.getCoordinatesLists());
  }

  private void checkValid(final MultiPolygon g) {
    for (int i = 0; i < g.getGeometryCount(); i++) {
      final Polygon p = (Polygon)g.getGeometry(i);
      checkInvalidCoordinates(p);
      if (validErr != null) {
        return;
      }
      checkClosedRings(p);
      if (validErr != null) {
        return;
      }
    }

    final GeometryGraph graph = new GeometryGraph(0, g);

    checkTooFewPoints(graph);
    if (validErr != null) {
      return;
    }
    checkConsistentArea(graph);
    if (validErr != null) {
      return;
    }
    if (!isSelfTouchingRingFormingHoleValid) {
      checkNoSelfIntersectingRings(graph);
      if (validErr != null) {
        return;
      }
    }
    for (int i = 0; i < g.getGeometryCount(); i++) {
      final Polygon p = (Polygon)g.getGeometry(i);
      checkHolesInShell(p, graph);
      if (validErr != null) {
        return;
      }
    }
    for (int i = 0; i < g.getGeometryCount(); i++) {
      final Polygon p = (Polygon)g.getGeometry(i);
      checkHolesNotNested(p, graph);
      if (validErr != null) {
        return;
      }
    }
    checkShellsNotNested(g, graph);
    if (validErr != null) {
      return;
    }
    checkConnectedInteriors(graph);
  }

  /**
   * Checks validity of a Point.
   */
  private void checkValid(final Point g) {
    checkInvalidCoordinates(g);
  }

  /**
   * Checks the validity of a polygon. Sets the validErr flag.
   */
  private void checkValid(final Polygon g) {
    checkInvalidCoordinates(g);
    if (validErr != null) {
      return;
    }
    checkClosedRings(g);
    if (validErr != null) {
      return;
    }

    final GeometryGraph graph = new GeometryGraph(0, g);

    checkTooFewPoints(graph);
    if (validErr != null) {
      return;
    }
    checkConsistentArea(graph);
    if (validErr != null) {
      return;
    }

    if (!isSelfTouchingRingFormingHoleValid) {
      checkNoSelfIntersectingRings(graph);
      if (validErr != null) {
        return;
      }
    }
    checkHolesInShell(g, graph);
    if (validErr != null) {
      return;
    }
    // SLOWcheckHolesNotNested(g);
    checkHolesNotNested(g, graph);
    if (validErr != null) {
      return;
    }
    checkConnectedInteriors(graph);
  }

  public TopologyValidationError getValidationError() {
    checkValid(parentGeometry);
    return validErr;
  }

  public boolean isValid() {
    checkValid(parentGeometry);
    return validErr == null;
  }

  /**
   * Sets whether polygons using <b>Self-Touching Rings</b> to form holes are
   * reported as valid. If this flag is set, the following Self-Touching
   * conditions are treated as being valid:
   * <ul>
   * <li>the shell ring self-touches to create a hole touching the shell
   * <li>a hole ring self-touches to create two holes touching at a point
   * </ul>
   * <p>
   * The default (following the OGC SFS standard) is that this condition is
   * <b>not</b> valid (<code>false</code>).
   * <p>
   * This does not affect whether Self-Touching Rings disconnecting the polygon
   * interior are considered valid (these are considered to be <b>invalid</b>
   * under the SFS, and many other spatial models as well). This includes
   * "bow-tie" shells, which self-touch at a single point causing the interior
   * to be disconnected, and "C-shaped" holes which self-touch at a single point
   * causing an island to be formed.
   * 
   * @param isValid states whether geometry with this condition is valid
   */
  public void setSelfTouchingRingFormingHoleValid(final boolean isValid) {
    isSelfTouchingRingFormingHoleValid = isValid;
  }

}
