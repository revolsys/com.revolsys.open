package com.revolsys.gis.jts;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.revolsys.gis.model.coordinates.CoordinatesUtil;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.jts.algorithm.LineIntersector;
import com.revolsys.jts.algorithm.RobustLineIntersector;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.MultiPoint;
import com.revolsys.jts.geom.MultiPolygon;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.Polygon;
import com.revolsys.jts.geomgraph.Edge;
import com.revolsys.jts.geomgraph.EdgeIntersection;
import com.revolsys.jts.geomgraph.GeometryGraph;
import com.revolsys.jts.geomgraph.index.EdgeSetIntersector;
import com.revolsys.jts.geomgraph.index.SimpleMCSweepLineIntersector;

public class IsSimpleOp {
  private static class EndpointInfo {
    Coordinates pt;

    boolean isClosed;

    int degree;

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

  public static SegmentIntersector computeIntersections(
    final GeometryGraph graph, final LineIntersector li, final boolean ringBased) {
    final SegmentIntersector si = new SegmentIntersector(li, true, false);
    final EdgeSetIntersector esi = new SimpleMCSweepLineIntersector();
    final List<Edge> edges = new ArrayList<Edge>();
    final Iterator<Edge> edgeIter = graph.getEdgeIterator();
    while (edgeIter.hasNext()) {
      final Edge edge = edgeIter.next();
      edges.add(edge);
    }
    // optimized test for Polygons and Rings
    if (ringBased) {
      esi.computeIntersections(edges, si, false);
    } else {
      esi.computeIntersections(edges, si, true);
    }
    return si;
  }

  private final Geometry geometry;

  private final List<Coordinates> nonSimplePoints = new ArrayList<Coordinates>();

  public IsSimpleOp(final Geometry geometry) {
    this.geometry = geometry;
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

  private void addNonSimplePoint(final Coordinates coordinate) {
    nonSimplePoints.add(new DoubleCoordinates(CoordinatesUtil.get(coordinate),
      2));
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
    boolean hasIntersection = false;
    final Map<Coordinates, EndpointInfo> endPoints = new TreeMap<>();
    for (final Iterator i = graph.getEdgeIterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      final int maxSegmentIndex = e.getMaximumSegmentIndex();
      final boolean isClosed = e.isClosed();
      final Coordinates p0 = e.getCoordinate(0);
      addEndpoint(endPoints, p0, isClosed);
      final Coordinates p1 = e.getCoordinate(e.getNumPoints() - 1);
      addEndpoint(endPoints, p1, isClosed);
    }

    for (final Iterator i = endPoints.values().iterator(); i.hasNext();) {
      final EndpointInfo eiInfo = (EndpointInfo)i.next();
      if (eiInfo.isClosed && eiInfo.degree != 2) {
        addNonSimplePoint(eiInfo.getCoordinate());
        hasIntersection = true;
      }
    }
    return hasIntersection;
  }

  /**
   * For all edges, check if there are any intersections which are NOT at an endpoint.
   * The Geometry is not simple if there are intersections not at endpoints.
   */
  private boolean hasNonEndpointIntersection(final GeometryGraph graph) {
    boolean hasIntersection = false;
    for (final Iterator i = graph.getEdgeIterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      final int maxSegmentIndex = e.getMaximumSegmentIndex();
      for (final Iterator eiIt = e.getEdgeIntersectionList().iterator(); eiIt.hasNext();) {
        final EdgeIntersection ei = (EdgeIntersection)eiIt.next();
        if (!ei.isEndPoint(maxSegmentIndex)) {
          final Coordinates coordinate = ei.getCoordinate();
          addNonSimplePoint(coordinate);
          hasIntersection = true;
        }
      }
    }
    return hasIntersection;
  }

  /**
   * Tests whether the geometry is simple.
   *
   * @return true if the geometry is simple
   */
  public boolean isSimple() {
    if (geometry.isEmpty()) {
      return true;
    } else if (geometry instanceof LineString) {
      return isSimpleLinearGeometry(geometry);
    } else if (geometry instanceof MultiLineString) {
      return isSimpleLinearGeometry(geometry);
    } else if (geometry instanceof MultiPoint) {
      return isSimple((MultiPoint)geometry);
    }
    // all other geometry types are simple by definition
    return true;
  }

  private boolean isSimple(final MultiPoint multiPoint) {
    if (multiPoint.isEmpty()) {
      return true;
    } else {
      boolean simple = true;
      final Set<Coordinates> points = new TreeSet<Coordinates>();
      for (int i = 0; i < multiPoint.getNumGeometries(); i++) {
        final Point point = (Point)multiPoint.getGeometry(i);
        final Coordinates coordinates = new DoubleCoordinates(
          CoordinatesUtil.getInstance(point), 2);
        if (points.contains(coordinates)) {
          nonSimplePoints.add(coordinates);
          simple = false;
        }
        points.add(coordinates);
      }
      return simple;
    }
  }

  private boolean isSimpleLinearGeometry(final Geometry geom) {
    final GeometryGraph graph = new GeometryGraph(0, geom);
    final LineIntersector li = new RobustLineIntersector();
    final boolean ringBased = geom instanceof LinearRing
      || geom instanceof Polygon || geom instanceof MultiPolygon;
    final SegmentIntersector si = computeIntersections(graph, li, ringBased);

    // if no self-intersection, must be simple
    if (si.hasIntersection()) {
      final List<Coordinates> properIntersections = si.getProperIntersections();
      if (properIntersections.isEmpty()) {
        if (si.hasProperIntersection()) {
          addNonSimplePoint(si.getProperIntersectionPoint());
          return false;
        } else if (hasNonEndpointIntersection(graph)) {
          return false;
        } else if (hasClosedEndpointIntersection(graph)) {
          return false;
        }
      } else {
        nonSimplePoints.addAll(properIntersections);
        return false;
      }
    } else {
      return true;
    }
    return true;
  }

}
