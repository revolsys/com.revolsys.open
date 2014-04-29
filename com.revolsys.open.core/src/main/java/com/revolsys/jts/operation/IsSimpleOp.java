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
package com.revolsys.jts.operation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.algorithm.BoundaryNodeRule;
import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Lineal;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geomgraph.Edge;
import com.revolsys.jts.geomgraph.EdgeIntersection;
import com.revolsys.jts.geomgraph.GeometryGraph;
import com.revolsys.jts.geomgraph.index.SegmentIntersector;

/**
 * Tests whether a <code>Geometry</code> is simple.
 * In general, the SFS specification of simplicity
 * follows the rule:
 * <ul>
 *    <li> A Geometry is simple if and only if the only self-intersections are at
 *    boundary points.
 * </ul>
 * <p>
 * Simplicity is defined for each {@link Geometry} type as follows:
 * <ul>
 * <li><b>Polygonal</b> geometries are simple by definition, so
 * <code>isSimple</code> trivially returns true.
 * (Note: this means that <tt>isSimple</tt> cannot be used to test 
 * for (invalid) self-intersections in <tt>Polygon</tt>s.  
 * In order to check if a <tt>Polygonal</tt> geometry has self-intersections,
 * use {@link Geometry#isValid()}).
 * <li><b>Linear</b> geometries are simple iff they do <i>not</i> self-intersect at interior points
 * (i.e. points other than boundary points).
 * This is equivalent to saying that no two linear components satisfy the SFS {@link Geometry#touches(Geometry)}
 * predicate. 
 * <li><b>Zero-dimensional (point)</b> geometries are simple if and only if they have no
 * repeated points.
 * <li><b>Empty</b> geometries are <i>always</i> simple, by definition
 * </ul>
 * For {@link Lineal} geometries the evaluation of simplicity  
 * can be customized by supplying a {@link BoundaryNodeRule} 
 * to define how boundary points are determined.
 * The default is the SFS-standard {@link BoundaryNodeRule#MOD2_BOUNDARY_RULE}.
 * Note that under the <tt>Mod-2</tt> rule, closed <tt>LineString</tt>s (rings)
 * will never satisfy the <tt>touches</tt> predicate at their endpoints, since these are
 * interior points, not boundary points. 
 * If it is required to test whether a set of <code>LineString</code>s touch
 * only at their endpoints, use <code>IsSimpleOp</code> with {@link BoundaryNodeRule#ENDPOINT_BOUNDARY_RULE}.
 * For example, this can be used to validate that a set of lines form a topologically valid
 * linear network.
 * 
 * @see BoundaryNodeRule
 *
 * @version 1.7
 */
public class IsSimpleOp {
  private static class EndpointInfo {
    int degree;

    boolean isClosed;

    Coordinates pt;

    public EndpointInfo(final Coordinates pt) {
      this.pt = pt;
      isClosed = false;
      degree = 0;
    }

    public void addEndpoint(final boolean isClosed) {
      degree++;
      this.isClosed |= isClosed;
    }

    public Coordinates getCoordinate() {
      return pt;
    }
  }

  private final Geometry geometry;

  private boolean isClosedEndpointsInInterior = true;

  private final List<Coordinates> nonSimplePoints = new ArrayList<Coordinates>();

  private boolean shortCircuit = true;

  /**
   * Creates a simplicity checker using the default SFS Mod-2 Boundary Node Rule
   *
   * @param geometry the geometry to test
   */
  public IsSimpleOp(final Geometry geometry) {
    this.geometry = geometry;
  }

  public IsSimpleOp(final Geometry geometry, final boolean shortCircuit) {
    this.geometry = geometry;
    this.shortCircuit = shortCircuit;
  }

  /**
   * Creates a simplicity checker using a given {@link BoundaryNodeRule}
   *
   * @param geometry the geometry to test
   * @param boundaryNodeRule the rule to use.
   */
  public IsSimpleOp(final Geometry geometry,
    final BoundaryNodeRule boundaryNodeRule) {
    this.geometry = geometry;
    isClosedEndpointsInInterior = !boundaryNodeRule.isInBoundary(2);
  }

  /**
   * Add an endpoint to the map, creating an entry for it if none exists
   */
  private void addEndpoint(final Map<Coordinates, EndpointInfo> endPoints,
    final Coordinates p, final boolean isClosed) {
    EndpointInfo eiInfo = endPoints.get(p);
    if (eiInfo == null) {
      eiInfo = new EndpointInfo(p);
      endPoints.put(p, eiInfo);
    }
    eiInfo.addEndpoint(isClosed);
  }

  /**
   * Gets a coordinate for the location where the geometry
   * fails to be simple. 
   * (i.e. where it has a non-boundary self-intersection).
   * {@link #isSimple} must be called before this method is called.
   *
   * @return a coordinate for the location of the non-boundary self-intersection
   * or null if the geometry is simple
   */
  public Coordinates getNonSimpleLocation() {
    if (nonSimplePoints.isEmpty()) {
      return null;
    } else {
      return nonSimplePoints.get(0);
    }
  }

  public List<Coordinates> getNonSimplePoints() {
    return nonSimplePoints;
  }

  /**
   * Tests that no edge intersection is the endpoint of a closed line.
   * This ensures that closed lines are not touched at their endpoint,
   * which is an interior point according to the Mod-2 rule
   * To check this we compute the degree of each endpoint.
   * The degree of endpoints of closed lines
   * must be exactly 2.
   */
  private boolean hasClosedEndpointIntersection(final GeometryGraph graph) {
    final Map<Coordinates, EndpointInfo> endPoints = new TreeMap<>();
    for (final Edge e : graph.edges()) {
      final boolean isClosed = e.isClosed();
      final Coordinates p0 = e.getCoordinate(0);
      addEndpoint(endPoints, p0, isClosed);
      final Coordinates p1 = e.getCoordinate(e.getNumPoints() - 1);
      addEndpoint(endPoints, p1, isClosed);
    }

    for (final EndpointInfo eiInfo : endPoints.values()) {
      if (eiInfo.isClosed && eiInfo.degree != 2) {
        nonSimplePoints.add(eiInfo.getCoordinate());
        return true;
      }
    }
    return false;
  }

  /**
   * For all edges, check if there are any intersections which are NOT at an endpoint.
   * The Geometry is not simple if there are intersections not at endpoints.
   */
  private boolean hasNonEndpointIntersection(final GeometryGraph graph) {
    for (final Edge edge : graph.edges()) {
      final int maxSegmentIndex = edge.getMaximumSegmentIndex();
      for (final EdgeIntersection ei : edge.getEdgeIntersectionList()) {
        if (!ei.isEndPoint(maxSegmentIndex)) {
          nonSimplePoints.add(ei.getCoordinate());
          return true;
        }
      }
    }
    return false;
  }

  private boolean isErrorReturn() {
    return shortCircuit && !nonSimplePoints.isEmpty();
  }

  /**
   * Tests whether the geometry is simple.
   *
   * @return true if the geometry is simple
   */
  public boolean isSimple() {
    nonSimplePoints.clear();
    return isSimple(geometry);
  }

  private boolean isSimple(final Geometry geometry) {
    if (geometry.isEmpty()) {
      return true;
    } else if (geometry instanceof Lineal) {
      return isSimple((Lineal)geometry);
    } else if (geometry instanceof Polygon) {
      return isSimple((Polygon)geometry);
    } else if (geometry instanceof MultiPoint) {
      return isSimple((MultiPoint)geometry);
    } else if (geometry instanceof GeometryCollection) {
      return isSimple((GeometryCollection)geometry);
    } else {
      return true;
    }
  }

  /**
   * Semantics for GeometryCollection is 
   * simple if all components are simple.
   * 
   * @param geom
   * @return true if the geometry is simple
   */
  private boolean isSimple(final GeometryCollection geom) {
    boolean simple = true;
    for (final Geometry part : geom.geometries()) {
      simple &= isSimple(part);
      if (isErrorReturn()) {
        return false;
      }
    }
    return simple;
  }

  private boolean isSimple(final Lineal geom) {
    final GeometryGraph graph = new GeometryGraph(0, geom);
    final LineIntersector li = new RobustLineIntersector();
    final SegmentIntersector si = graph.computeSelfNodes(li, true);
    // if no self-intersection, must be simple
    if (!si.hasIntersection()) {
      return true;
    }
    if (si.hasProperIntersection()) {
      nonSimplePoints.add(si.getProperIntersectionPoint());
      return false;
    }
    if (hasNonEndpointIntersection(graph)) {
      return false;
    }
    if (isClosedEndpointsInInterior) {
      if (hasClosedEndpointIntersection(graph)) {
        return false;
      }
    }
    return true;
  }

  private boolean isSimple(final MultiPoint mulitPoint) {
    boolean simple = true;
    final Set<Coordinates> points = new TreeSet<>();
    for (final Point point : mulitPoint.getPoints()) {
      final Coordinates coordinates = new DoubleCoordinates(point, 2);
      if (points.contains(coordinates)) {
        simple = false;
        nonSimplePoints.add(coordinates);
        if (!isErrorReturn()) {
          return false;
        }
      } else {
        points.add(coordinates);
      }
    }
    return simple;
  }

  /**
   * Computes simplicity for polygonal geometries.
   * Polygonal geometries are simple if and only if
   * all of their component rings are simple.
   * 
   * @param geom a Polygonal geometry
   * @return true if the geometry is simple
   */
  private boolean isSimple(final Polygon polygon) {
    boolean simple = true;
    for (final LineString ring : polygon.rings()) {
      simple &= isSimple(ring);
      if (isErrorReturn()) {
        return false;
      }
    }
    return simple;
  }
}
