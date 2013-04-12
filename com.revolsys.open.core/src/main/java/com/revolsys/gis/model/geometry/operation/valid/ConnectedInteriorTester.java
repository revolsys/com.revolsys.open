package com.revolsys.gis.model.geometry.operation.valid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryFactory;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.MultiPolygon;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.impl.GeometryFactoryImpl;
import com.revolsys.gis.model.geometry.operation.geomgraph.DirectedEdge;
import com.revolsys.gis.model.geometry.operation.geomgraph.Edge;
import com.revolsys.gis.model.geometry.operation.geomgraph.EdgeRing;
import com.revolsys.gis.model.geometry.operation.geomgraph.GeometryGraph;
import com.revolsys.gis.model.geometry.operation.geomgraph.PlanarGraph;
import com.revolsys.gis.model.geometry.operation.geomgraph.Position;
import com.revolsys.gis.model.geometry.operation.overlay.MaximalEdgeRing;
import com.revolsys.gis.model.geometry.operation.overlay.OverlayNodeFactory;
import com.vividsolutions.jts.geom.Location;
import com.vividsolutions.jts.util.Assert;

/**
 * This class tests that the interior of an area {@link Geometry}
 * ( {@link Polygon}  or {@link MultiPolygon} )
 * is connected.
 * This can happen if:
 * <ul>
 * <li>a shell self-intersects
 * <li>one or more holes form a connected chain touching a shell at two different points
 * <li>one or more holes form a ring around a subset of the interior
 * </ul>
 * If a disconnected situation is found the location of the problem is recorded.
 *
 * @version 1.7
 */
public class ConnectedInteriorTester {

  public static Coordinates findDifferentPoint(final CoordinatesList points,
    final Coordinates pt) {
    for (int i = 0; i < points.size(); i++) {
      final Coordinates point = points.get(i);
      if (!point.equals(pt)) {
        return point;
      }
    }
    return null;
  }

  private final GeometryFactory geometryFactory = GeometryFactoryImpl.getFactory();

  private final GeometryGraph geomGraph;

  // save a coordinate for any disconnected interior found
  // the coordinate will be somewhere on the ring surrounding the disconnected
  // interior
  private Coordinates disconnectedRingcoord;

  public ConnectedInteriorTester(final GeometryGraph geomGraph) {
    this.geomGraph = geomGraph;
  }

  /**
   * Form DirectedEdges in graph into Minimal EdgeRings.
   * (Minimal Edgerings must be used, because only they are guaranteed to provide
   * a correct isHole computation)
   */
  private List buildEdgeRings(final Collection dirEdges) {
    final List edgeRings = new ArrayList();
    for (final Iterator it = dirEdges.iterator(); it.hasNext();) {
      final DirectedEdge de = (DirectedEdge)it.next();
      // if this edge has not yet been processed
      if (de.isInResult() && de.getEdgeRing() == null) {
        final MaximalEdgeRing er = new MaximalEdgeRing(de, geometryFactory);

        er.linkDirectedEdgesForMinimalEdgeRings();
        final List minEdgeRings = er.buildMinimalRings();
        edgeRings.addAll(minEdgeRings);
      }
    }
    return edgeRings;
  }

  public Coordinates getCoordinate() {
    return disconnectedRingcoord;
  }

  /**
   * Check if any shell ring has an unvisited edge.
   * A shell ring is a ring which is not a hole and which has the interior
   * of the parent area on the RHS.
   * (Note that there may be non-hole rings with the interior on the LHS,
   * since the interior of holes will also be polygonized into CW rings
   * by the linkAllDirectedEdges() step)
   *
   * @return true if there is an unvisited edge in a non-hole ring
   */
  private boolean hasUnvisitedShellEdge(final List edgeRings) {
    for (int i = 0; i < edgeRings.size(); i++) {
      final EdgeRing er = (EdgeRing)edgeRings.get(i);
      // don't check hole rings
      if (er.isHole()) {
        continue;
      }
      final List edges = er.getEdges();
      DirectedEdge de = (DirectedEdge)edges.get(0);
      // don't check CW rings which are holes
      // (MD - this check may now be irrelevant)
      if (de.getLabel().getLocation(0, Position.RIGHT) != Location.INTERIOR) {
        continue;
      }

      /**
       * the edgeRing is CW ring which surrounds the INT of the area, so check all
       * edges have been visited.  If any are unvisited, this is a disconnected part of the interior
       */
      for (int j = 0; j < edges.size(); j++) {
        de = (DirectedEdge)edges.get(j);
        // Debug.print("visted? "); Debug.println(de);
        if (!de.isVisited()) {
          // Debug.print("not visited "); Debug.println(de);
          disconnectedRingcoord = de.getCoordinate();
          return true;
        }
      }
    }
    return false;
  }

  public boolean isInteriorsConnected() {
    // node the edges, in case holes touch the shell
    final List splitEdges = new ArrayList();
    geomGraph.computeSplitEdges(splitEdges);

    // form the edges into rings
    final PlanarGraph graph = new PlanarGraph(new OverlayNodeFactory());
    graph.addEdges(splitEdges);
    setInteriorEdgesInResult(graph);
    graph.linkResultDirectedEdges();
    final List edgeRings = buildEdgeRings(graph.getEdgeEnds());

    /**
     * Mark all the edges for the edgeRings corresponding to the shells
     * of the input polygons.  Note only ONE ring gets marked for each shell.
     */
    visitShellInteriors(geomGraph.getGeometry(), graph);

    /**
     * If there are any unvisited shell edges
     * (i.e. a ring which is not a hole and which has the interior
     * of the parent area on the RHS)
     * this means that one or more holes must have split the interior of the
     * polygon into at least two pieces.  The polygon is thus invalid.
     */
    return !hasUnvisitedShellEdge(edgeRings);
  }

  private void setInteriorEdgesInResult(final PlanarGraph graph) {
    for (final Iterator it = graph.getEdgeEnds().iterator(); it.hasNext();) {
      final DirectedEdge de = (DirectedEdge)it.next();
      if (de.getLabel().getLocation(0, Position.RIGHT) == Location.INTERIOR) {
        de.setInResult(true);
      }
    }
  }

  private void visitInteriorRing(final LineString ring, final PlanarGraph graph) {
    final CoordinatesList pts = ring;
    final Coordinates pt0 = pts.get(0);
    /**
     * Find first point in coord list different to initial point.
     * Need special check since the first point may be repeated.
     */
    final Coordinates pt1 = findDifferentPoint(pts, pt0);
    final Edge e = graph.findEdgeInSameDirection(pt0, pt1);
    final DirectedEdge de = (DirectedEdge)graph.findEdgeEnd(e);
    DirectedEdge intDe = null;
    if (de.getLabel().getLocation(0, Position.RIGHT) == Location.INTERIOR) {
      intDe = de;
    } else if (de.getSym().getLabel().getLocation(0, Position.RIGHT) == Location.INTERIOR) {
      intDe = de.getSym();
    }
    Assert.isTrue(intDe != null, "unable to find dirEdge with Interior on RHS");

    visitLinkedDirectedEdges(intDe);
  }

  protected void visitLinkedDirectedEdges(final DirectedEdge start) {
    final DirectedEdge startDe = start;
    DirectedEdge de = start;
    do {
      Assert.isTrue(de != null, "found null Directed Edge");
      de.setVisited(true);
      de = de.getNext();
    } while (de != startDe);
  }

  /**
   * Mark all the edges for the edgeRings corresponding to the shells
   * of the input polygons.
   * Only ONE ring gets marked for each shell - if there are others which remain unmarked
   * this indicates a disconnected interior.
   */
  private void visitShellInteriors(final Geometry g, final PlanarGraph graph) {
    if (g instanceof Polygon) {
      final Polygon p = (Polygon)g;
      visitInteriorRing(p.getExteriorRing(), graph);
    }
    if (g instanceof MultiPolygon) {
      final MultiPolygon mp = (MultiPolygon)g;
      for (int i = 0; i < mp.getGeometryCount(); i++) {
        final Polygon p = (Polygon)mp.getGeometry(i);
        visitInteriorRing(p.getExteriorRing(), graph);
      }
    }
  }
}
