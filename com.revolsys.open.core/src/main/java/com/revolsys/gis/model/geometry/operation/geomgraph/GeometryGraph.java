package com.revolsys.gis.model.geometry.operation.geomgraph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.revolsys.gis.model.coordinates.Coordinates;
import com.revolsys.gis.model.coordinates.list.CoordinatesList;
import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.gis.model.geometry.MultiLineString;
import com.revolsys.gis.model.geometry.MultiLinearRing;
import com.revolsys.gis.model.geometry.MultiPoint;
import com.revolsys.gis.model.geometry.MultiPolygon;
import com.revolsys.gis.model.geometry.Point;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.Polygonal;
import com.revolsys.gis.model.geometry.algorithm.locate.IndexedPointInAreaLocator;
import com.revolsys.gis.model.geometry.algorithm.locate.PointOnGeometryLocator;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.EdgeSetIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.LineIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.PointLocator;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.SegmentIntersector;
import com.revolsys.gis.model.geometry.operation.geomgraph.index.SimpleMCSweepLineIntersector;
import com.revolsys.jts.algorithm.BoundaryNodeRule;
import com.revolsys.jts.geom.Location;
import com.revolsys.jts.util.Assert;

/**
 * A GeometryGraph is a graph that models a given Geometry
 * 
 * @version 1.7
 */
public class GeometryGraph extends PlanarGraph {
  /**
   * This method implements the Boundary Determination Rule for determining
   * whether a component (node or edge) that appears multiple times in elements
   * of a MultiGeometry is in the boundary or the interior of the Geometry <br>
   * The SFS uses the "Mod-2 Rule", which this function implements <br>
   * An alternative (and possibly more intuitive) rule would be the
   * "At Most One Rule": isInBoundary = (componentCount == 1)
   */
  /*
   * public static boolean isInBoundary(int boundaryCount) { // the "Mod-2 Rule"
   * return boundaryCount % 2 == 1; } public static int determineBoundary(int
   * boundaryCount) { return isInBoundary(boundaryCount) ? Location.BOUNDARY :
   * Location.INTERIOR; }
   */

  public static int determineBoundary(final BoundaryNodeRule boundaryNodeRule,
    final int boundaryCount) {
    return boundaryNodeRule.isInBoundary(boundaryCount) ? Location.BOUNDARY
      : Location.INTERIOR;
  }

  private final Geometry parentGeom;

  /**
   * The lineEdgeMap is a map of the linestring components of the parentGeometry
   * to the edges which are derived from them. This is used to efficiently
   * perform findEdge queries
   */
  private final Map lineEdgeMap = new HashMap();

  private BoundaryNodeRule boundaryNodeRule = null;

  /**
   * If this flag is true, the Boundary Determination Rule will used when
   * deciding whether nodes are in the boundary or not
   */
  private boolean useBoundaryDeterminationRule = true;

  private final int argIndex; // the index of this geometry as an argument to a
                              // spatial function (used for labelling)

  private Collection boundaryNodes;

  private boolean hasTooFewPoints = false;

  private Coordinates invalidPoint = null;

  private PointOnGeometryLocator areaPtLocator = null;

  // for use if geometry is not Polygonal
  private final PointLocator ptLocator;

  public GeometryGraph(final int argIndex, final Geometry parentGeom) {
    this(argIndex, parentGeom, BoundaryNodeRule.OGC_SFS_BOUNDARY_RULE);
  }

  public GeometryGraph(final int argIndex, final Geometry parentGeom,
    final BoundaryNodeRule boundaryNodeRule) {
    this.argIndex = argIndex;
    this.parentGeom = parentGeom;
    this.boundaryNodeRule = boundaryNodeRule;
    if (parentGeom != null) {
      // precisionModel = parentGeom.getPrecisionModel();
      // SRID = parentGeom.getSRID();
      add(parentGeom);
    }
    ptLocator = new PointLocator(parentGeom.getGeometryFactory());
  }

  private void add(final Geometry g) {
    if (g != null && !g.isEmpty()) {
      // check if this Geometry should obey the Boundary Determination Rule
      // all collections except MultiPolygons obey the rule
      if (g instanceof MultiPolygon) {
        useBoundaryDeterminationRule = false;
      }

      if (g instanceof Polygon) {
        addPolygon((Polygon)g);
      } else if (g instanceof LineString) {
        addLineString((LineString)g);
      } else if (g instanceof Point) {
        addPoint((Point)g);
      } else if (g instanceof MultiPoint) {
        addCollection((MultiPoint)g);
      } else if (g instanceof MultiLineString) {
        addCollection((MultiLineString)g);
      } else if (g instanceof MultiPolygon) {
        addCollection((MultiPolygon)g);
      } else if (g instanceof GeometryCollection) {
        addCollection((GeometryCollection)g);
      } else {
        throw new UnsupportedOperationException(g.getClass().getName());
      }
    }
  }

  private void addCollection(final GeometryCollection gc) {
    for (final Geometry g : gc.getGeometries()) {
      add(g);
    }
  }

  /**
   * Add an Edge computed externally. The label on the Edge is assumed to be
   * correct.
   */
  public void addEdge(final Edge e) {
    insertEdge(e);
    final CoordinatesList coord = e.getCoordinates();
    // insert the endpoint as a node, to mark that it is on the boundary
    insertPoint(argIndex, coord.get(0), Location.BOUNDARY);
    insertPoint(argIndex, coord.get(coord.size() - 1), Location.BOUNDARY);
  }

  private void addLineString(final LineString line) {
    final CoordinatesList coord = CoordinatesListUtil.removeRepeatedPoints(line);

    if (coord.size() < 2) {
      hasTooFewPoints = true;
      invalidPoint = coord.get(0);
      return;
    }

    // add the edge for the LineString
    // line edges do not have locations for their left and right sides
    final Edge e = new Edge(coord, new Label(argIndex, Location.INTERIOR));
    lineEdgeMap.put(line, e);
    insertEdge(e);
    /**
     * Add the boundary points of the LineString, if any. Even if the LineString
     * is closed, add both points as if they were endpoints. This allows for the
     * case that the node already exists and is a boundary point.
     */
    Assert.isTrue(coord.size() >= 2, "found LineString with single point");
    insertBoundaryPoint(argIndex, coord.get(0));
    insertBoundaryPoint(argIndex, coord.get(coord.size() - 1));

  }

  /**
   * Add a point computed externally. The point is assumed to be a Point
   * Geometry part, which has a location of INTERIOR.
   */
  public void addPoint(final Coordinates pt) {
    insertPoint(argIndex, pt, Location.INTERIOR);
  }

  /**
   * Add a Point to the graph.
   */
  private void addPoint(final Point p) {
    final Coordinates coord = p;
    insertPoint(argIndex, coord, Location.INTERIOR);
  }

  private void addPolygon(final Polygon polygon) {
    final MultiLinearRing rings = polygon.getRings();
    final LinearRing exteriorRing = polygon.getExteriorRing();
    addPolygonRing(exteriorRing, Location.EXTERIOR, Location.INTERIOR);

    for (int i = 1; i < rings.getGeometryCount(); i++) {
      final LinearRing hole = rings.getGeometry(i);

      // Holes are topologically labelled opposite to the shell, since
      // the interior of the polygon lies on their opposite side
      // (on the left, if the hole is oriented CW)
      addPolygonRing(hole, Location.INTERIOR, Location.EXTERIOR);
    }
  }

  /**
   * Adds a polygon ring to the graph. Empty rings are ignored. The left and
   * right topological location arguments assume that the ring is oriented CW.
   * If the ring is in the opposite orientation, the left and right locations
   * must be interchanged.
   */
  private void addPolygonRing(final LinearRing lr, final int cwLeft,
    final int cwRight) {
    // don't bother adding empty holes
    if (lr.isEmpty()) {
      return;
    }

    final CoordinatesList coord = CoordinatesListUtil.removeRepeatedPoints(lr);

    if (coord.size() < 4) {
      hasTooFewPoints = true;
      invalidPoint = coord.get(0);
      return;
    }

    int left = cwLeft;
    int right = cwRight;
    if (CoordinatesListUtil.isCCW(coord)) {
      left = cwRight;
      right = cwLeft;
    }
    final Edge e = new Edge(coord, new Label(argIndex, Location.BOUNDARY, left,
      right));
    lineEdgeMap.put(lr, e);

    insertEdge(e);
    // insert the endpoint as a node, to mark that it is on the boundary
    insertPoint(argIndex, coord.get(0), Location.BOUNDARY);
  }

  /**
   * Add a node for a self-intersection. If the node is a potential boundary
   * node (e.g. came from an edge which is a boundary) then insert it as a
   * potential boundary node. Otherwise, just add it as a regular node.
   */
  private void addSelfIntersectionNode(final int argIndex,
    final Coordinates coord, final int loc) {
    // if this node is already a boundary node, don't change it
    if (isBoundaryNode(argIndex, coord)) {
      return;
    }
    if (loc == Location.BOUNDARY && useBoundaryDeterminationRule) {
      insertBoundaryPoint(argIndex, coord);
    } else {
      insertPoint(argIndex, coord, loc);
    }
  }

  private void addSelfIntersectionNodes(final int argIndex) {
    for (final Iterator i = edges.iterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      final int eLoc = e.getLabel().getLocation(argIndex);
      for (final Iterator eiIt = e.eiList.iterator(); eiIt.hasNext();) {
        final EdgeIntersection ei = (EdgeIntersection)eiIt.next();
        addSelfIntersectionNode(argIndex, ei.coord, eLoc);
      }
    }
  }

  public SegmentIntersector computeEdgeIntersections(final GeometryGraph g,
    final LineIntersector li, final boolean includeProper) {
    final SegmentIntersector si = new SegmentIntersector(li, includeProper,
      true);
    si.setBoundaryNodes(this.getBoundaryNodes(), g.getBoundaryNodes());

    final EdgeSetIntersector esi = createEdgeSetIntersector();
    esi.computeIntersections(edges, g.edges, si);
    /*
     * for (Iterator i = g.edges.iterator(); i.hasNext();) { Edge e = (Edge)
     * i.next(); Debug.print(e.getEdgeIntersectionList()); }
     */
    return si;
  }

  /**
   * Compute self-nodes, taking advantage of the Geometry type to minimize the
   * number of intersection tests. (E.g. rings are not tested for
   * self-intersection, since they are assumed to be valid).
   * 
   * @param li the LineIntersector to use
   * @param computeRingSelfNodes if <false>, intersection checks are optimized
   *          to not test rings for self-intersection
   * @return the SegmentIntersector used, containing information about the
   *         intersections found
   */
  public SegmentIntersector computeSelfNodes(final LineIntersector li,
    final boolean computeRingSelfNodes) {
    final SegmentIntersector si = new SegmentIntersector(li, true, false);
    final EdgeSetIntersector esi = createEdgeSetIntersector();
    // optimized test for Polygons and Rings
    if (!computeRingSelfNodes
      && (parentGeom instanceof LinearRing || parentGeom instanceof Polygon || parentGeom instanceof MultiPolygon)) {
      esi.computeIntersections(edges, si, false);
    } else {
      esi.computeIntersections(edges, si, true);
    }
    // System.out.println("SegmentIntersector # tests = " + si.numTests);
    addSelfIntersectionNodes(argIndex);
    return si;
  }

  public void computeSplitEdges(final List edgelist) {
    for (final Iterator i = edges.iterator(); i.hasNext();) {
      final Edge e = (Edge)i.next();
      e.eiList.addSplitEdges(edgelist);
    }
  }

  private EdgeSetIntersector createEdgeSetIntersector() {
    // various options for computing intersections, from slowest to fastest

    // private EdgeSetIntersector esi = new SimpleEdgeSetIntersector();
    // private EdgeSetIntersector esi = new MonotoneChainIntersector();
    // private EdgeSetIntersector esi = new NonReversingChainIntersector();
    // private EdgeSetIntersector esi = new SimpleSweepLineIntersector();
    // private EdgeSetIntersector esi = new MCSweepLineIntersector();

    // return new SimpleEdgeSetIntersector();
    return new SimpleMCSweepLineIntersector();
  }

  public Edge findEdge(final LineString line) {
    return (Edge)lineEdgeMap.get(line);
  }

  public BoundaryNodeRule getBoundaryNodeRule() {
    return boundaryNodeRule;
  }

  public Collection getBoundaryNodes() {
    if (boundaryNodes == null) {
      boundaryNodes = nodes.getBoundaryNodes(argIndex);
    }
    return boundaryNodes;
  }

  public Coordinates[] getBoundaryPoints() {
    final Collection coll = getBoundaryNodes();
    final Coordinates[] pts = new Coordinates[coll.size()];
    int i = 0;
    for (final Iterator it = coll.iterator(); it.hasNext();) {
      final Node node = (Node)it.next();
      pts[i++] = node.getCoordinate().cloneCoordinates();
    }
    return pts;
  }

  public Geometry getGeometry() {
    return parentGeom;
  }

  public Coordinates getInvalidPoint() {
    return invalidPoint;
  }

  /**
   * This constructor is used by clients that wish to add Edges explicitly,
   * rather than adding a Geometry. (An example is BufferOp).
   */
  // no longer used
  // public GeometryGraph(int argIndex, PrecisionModel precisionModel, int SRID)
  // {
  // this(argIndex, null);
  // this.precisionModel = precisionModel;
  // this.SRID = SRID;
  // }
  // public PrecisionModel getPrecisionModel()
  // {
  // return precisionModel;
  // }
  // public int getSRID() { return SRID; }

  public boolean hasTooFewPoints() {
    return hasTooFewPoints;
  }

  /**
   * Adds candidate boundary points using the current {@link BoundaryNodeRule}.
   * This is used to add the boundary points of dim-1 geometries
   * (Curves/MultiCurves).
   */
  private void insertBoundaryPoint(final int argIndex, final Coordinates coord) {
    final Node n = nodes.addNode(coord);
    final Label lbl = n.getLabel();
    // the new point to insert is on a boundary
    int boundaryCount = 1;
    // determine the current location for the point (if any)
    int loc = Location.NONE;
    if (lbl != null) {
      loc = lbl.getLocation(argIndex, Position.ON);
    }
    if (loc == Location.BOUNDARY) {
      boundaryCount++;
    }

    // determine the boundary status of the point according to the Boundary
    // Determination Rule
    final int newLoc = determineBoundary(boundaryNodeRule, boundaryCount);
    lbl.setLocation(argIndex, newLoc);
  }

  private void insertPoint(final int argIndex, final Coordinates coord,
    final int onLocation) {
    final Node n = nodes.addNode(coord);
    final Label lbl = n.getLabel();
    if (lbl == null) {
      n.label = new Label(argIndex, onLocation);
    } else {
      lbl.setLocation(argIndex, onLocation);
    }
  }

  // MD - experimental for now
  /**
   * Determines the {@link Location} of the given {@link Coordinates} in this
   * geometry.
   * 
   * @param p the point to test
   * @return the location of the point in the geometry
   */
  public int locate(final Coordinates pt) {
    if (parentGeom instanceof Polygonal && parentGeom.getGeometryCount() > 50) {
      // lazily init point locator
      if (areaPtLocator == null) {
        areaPtLocator = new IndexedPointInAreaLocator(parentGeom);
      }
      return areaPtLocator.locate(pt).getIndex();
    }
    return ptLocator.locate(pt, parentGeom);
  }
}
