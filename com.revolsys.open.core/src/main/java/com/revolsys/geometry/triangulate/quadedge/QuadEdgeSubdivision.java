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

package com.revolsys.geometry.triangulate.quadedge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import com.revolsys.elevation.tin.TriangleConsumer;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Lineal;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygonal;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.model.Triangle;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.geometry.model.impl.TriangleDoubleXYZ;

/**
 * A class that contains the {@link QuadEdge}s representing a planar
 * subdivision that models a triangulation.
 * The subdivision is constructed using the
 * quadedge algebra defined in the classs {@link QuadEdge}.
 * All metric calculations
 * are done in the {@link PointDoubleXYZ} class.
 * In addition to a triangulation, subdivisions
 * support extraction of Voronoi diagrams.
 * This is easily accomplished, since the Voronoi diagram is the dual
 * of the Delaunay triangulation.
 * <p>
 * Subdivisions can be provided with a tolerance value. Inserted vertices which
 * are closer than this value to vertices already in the subdivision will be
 * ignored. Using a suitable tolerance value can prevent robustness failures
 * from happening during Delaunay triangulation.
 * <p>
 * Subdivisions maintain a <b>frame</b> triangle around the client-created
 * edges. The frame is used to provide a bounded "container" for all edges
 * within a TIN. Normally the frame edges, frame connecting edges, and frame
 * triangles are not included in client processing.
 *
 * @author David Skea
 * @author Martin Davis
 */
public class QuadEdgeSubdivision {

  /**
   * Gets the edges for the triangle to the left of the given {@link QuadEdge}.
   *
   * @param startQE
   * @param triEdge
   *
   * @throws IllegalArgumentException
   *           if the edges do not form a triangle
   */
  public static void getTriangleEdges(final QuadEdge startQE, final QuadEdge[] triEdge) {
    triEdge[0] = startQE;
    triEdge[1] = triEdge[0].lNext();
    triEdge[2] = triEdge[1].lNext();
    if (triEdge[2].lNext() != triEdge[0]) {
      throw new IllegalArgumentException("Edges do not form a triangle");
    }
  }

  private BoundingBox frameBoundingBox;

  private final Point[] frameVertex = new Point[3];

  private double[] frameCoordinates;

  // private Set quadEdges = new HashSet();
  private final List<QuadEdge> quadEdges = new ArrayList<>();

  private final QuadEdge startingEdge;

  private final GeometryFactory geometryFactory;

  private QuadEdge lastEdge = null;

  /**
   * Creates a new instance of a quad-edge subdivision based on a frame triangle
   * that encloses a supplied bounding box. A new super-bounding box that
   * contains the triangle is computed and stored.
   *
   * @param bounds
   *          the bouding box to surround
   * @param tolerance
   *          the tolerance value for determining if two sites are equal
   */
  public QuadEdgeSubdivision(final double[] bounds, final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;

    newFrame(bounds);

    this.startingEdge = initSubdiv();
    initLocator();
  }

  /**
   * Creates a new QuadEdge connecting the destination of a to the origin of b,
   * in such a way that all three have the same left face after the connection
   * is complete. The quadedge is recorded in the edges list.
   *
   * @param a
   * @param b
   * @return a quadedge
   */
  public QuadEdge connect(final QuadEdge a, final QuadEdge b) {
    final QuadEdge q = QuadEdge.connect(a, b);
    this.quadEdges.add(q);
    return q;
  }

  /**
   * Deletes a quadedge from the subdivision. Linked quadedges are updated to
   * reflect the deletion.
   *
   * @param e
   *          the quadedge to delete
   */
  public void delete(final QuadEdge e) {
    QuadEdge.splice(e, e.oPrev());
    QuadEdge.splice(e.sym(), e.sym().oPrev());

    final QuadEdge eSym = e.sym();
    final QuadEdge eRot = e.rot();
    final QuadEdge eRotSym = e.rot().sym();

    // this is inefficient on an ArrayList, but this method should be called
    // infrequently
    this.quadEdges.remove(e);
    this.quadEdges.remove(eSym);
    this.quadEdges.remove(eRot);
    this.quadEdges.remove(eRotSym);

    e.delete();
    eSym.delete();
    eRot.delete();
    eRotSym.delete();
  }

  /**
   * Stores the edges for a visited triangle. Also pushes sym (neighbour) edges
   * on stack to visit later.
   *
   * @param edge
   * @param edgeStack
   * @return the visited triangle edges
   * or null if the triangle should not be visited (for instance, if it is
   *         outer)
   */
  private boolean fetchTriangleToVisit(final QuadEdge edge, final Deque<QuadEdge> edgeStack,
    final Set<QuadEdge> visitedEdges, final double[] coordinates) {
    QuadEdge currentEdge = edge;
    boolean isFrame = false;
    int offset = 0;
    do {
      final Point fromPoint = currentEdge.getFromPoint();
      coordinates[offset++] = fromPoint.getX();
      coordinates[offset++] = fromPoint.getY();
      coordinates[offset++] = fromPoint.getZ();
      if (isFrameEdge(currentEdge)) {
        isFrame = true;
      }

      // push sym edges to visit next
      final QuadEdge sym = currentEdge.sym();
      if (!visitedEdges.contains(sym)) {
        edgeStack.push(sym);
      }

      // mark this edge as visited
      visitedEdges.add(currentEdge);

      currentEdge = currentEdge.lNext();
    } while (currentEdge != edge);

    if (isFrame) {
      return false;
    } else {
      return true;
    }
  }

  public void forEachTriangle(final TriangleConsumer action) {
    final double[] coordinates = new double[9];
    final Deque<QuadEdge> edgeStack = new LinkedList<>();
    edgeStack.push(this.startingEdge);

    final Set<QuadEdge> visitedEdges = new HashSet<>();

    while (!edgeStack.isEmpty()) {
      final QuadEdge edge = edgeStack.pop();
      if (!visitedEdges.contains(edge)) {
        if (fetchTriangleToVisit(edge, edgeStack, visitedEdges, coordinates)) {
          final double x1 = coordinates[0];
          final double y1 = coordinates[1];
          final double z1 = coordinates[2];

          final double x2 = coordinates[3];
          final double y2 = coordinates[4];
          final double z2 = coordinates[5];

          final double x3 = coordinates[6];
          final double y3 = coordinates[7];
          final double z3 = coordinates[8];
          action.accept(x1, y1, z1, x2, y2, z2, x3, y3, z3);
        }
      }
    }
  }

  /**
   * Gets the collection of base {@link QuadEdge}s (one for every pair of
   * vertices which is connected).
   *
   * @return a collection of QuadEdges
   */
  public Collection<QuadEdge> getEdges() {
    return this.quadEdges;
  }

  /**
   * Gets the geometry for the edges in the subdivision as a {@link Lineal}
   * containing 2-point lines.
   *
   * @param geomFact the GeometryFactory to use
   * @return a MultiLineString
   */
  public Geometry getEdges(final GeometryFactory geomFact) {
    final List<QuadEdge> quadEdges = getPrimaryEdges(false);
    final LineString[] edges = new LineString[quadEdges.size()];
    int i = 0;
    for (final QuadEdge qe : quadEdges) {
      edges[i++] = geomFact.lineString(qe.getFromPoint(), qe.getToPoint());
    }
    return geomFact.lineal(edges);
  }

  /**
   * Gets the envelope of the Subdivision (including the frame).
   *
   * @return the envelope
   */
  public BoundingBox getEnvelope() {
    return this.frameBoundingBox;
  }

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  /**
   * Gets all primary quadedges in the subdivision.
   * A primary edge is a {@link QuadEdge}
   * which occupies the 0'th position in its array of associated quadedges.
   * These provide the unique geometric edges of the triangulation.
   *
   * @param includeFrame true if the frame edges are to be included
   * @return a List of QuadEdges
   */
  public List<QuadEdge> getPrimaryEdges(final boolean includeFrame) {
    final List<QuadEdge> edges = new ArrayList<>();
    final Stack<QuadEdge> edgeStack = new Stack<>();
    edgeStack.push(this.startingEdge);

    final Set<QuadEdge> visitedEdges = new HashSet<>();

    while (!edgeStack.empty()) {
      final QuadEdge edge = edgeStack.pop();
      if (!visitedEdges.contains(edge)) {
        final QuadEdge priQE = edge.getPrimary();

        if (includeFrame || !isFrameEdge(priQE)) {
          edges.add(priQE);
        }

        edgeStack.push(edge.getFromNextEdge());
        edgeStack.push(edge.sym().getFromNextEdge());

        visitedEdges.add(edge);
        visitedEdges.add(edge.sym());
      }
    }
    return edges;
  }

  public Polygonal getTriangles() {
    return getTriangles(this.geometryFactory);
  }

  /**
   * Gets the geometry for the triangles in a triangulated subdivision as a {@link Polygonal}.
   *
   * @param geometryFactory the GeometryFactory to use
   * @return a GeometryCollection of triangular Polygons
   */
  public Polygonal getTriangles(final GeometryFactory geometryFactory) {
    final List<Triangle> triangles = new ArrayList<>();
    forEachTriangle((final double x1, final double y1, final double z1, final double x2,
      final double y2, final double z2, final double x3, final double y3, final double z3) -> {
      final Triangle triangle = new TriangleDoubleXYZ(x1, y1, z1, x2, y2, z2, x3, y3, z3);
      triangles.add(triangle);
    });
    // final List<Point[]> triPtsList = getTriangleCoordinates(false);
    // final Polygon[] triangles = new Polygon[triPtsList.size()];
    // int i = 0;
    // for (final Point[] triPt : triPtsList) {
    // triangles[i++] = geometryFactory.polygon(geometryFactory.linearRing(triPt));
    // }
    return geometryFactory.polygonal(triangles);
  }

  private void initLocator() {
    this.lastEdge = this.quadEdges.get(0);
  }

  private QuadEdge initSubdiv() {
    // build initial subdivision from frame
    final QuadEdge ea = makeEdge(this.frameVertex[0], this.frameVertex[1]);
    final QuadEdge eb = makeEdge(this.frameVertex[1], this.frameVertex[2]);
    QuadEdge.splice(ea.sym(), eb);
    final QuadEdge ec = makeEdge(this.frameVertex[2], this.frameVertex[0]);
    QuadEdge.splice(eb.sym(), ec);
    QuadEdge.splice(ec.sym(), ea);
    return ea;
  }

  /**
   * Tests whether a QuadEdge is an edge incident on a frame triangle vertex.
   *
   * @param edge
   *          the edge to test
   * @return true if the edge is connected to the frame triangle
   */
  public boolean isFrameEdge(final QuadEdge edge) {
    final double[] frameCoordinates = this.frameCoordinates;
    for (int vertexIndex = 0; vertexIndex < 2; vertexIndex++) {
      final Point point = edge.getPoint(vertexIndex);
      final double x = point.getX();
      final double y = point.getY();
      for (int coordinateIndex = 0; coordinateIndex < 6;) {
        if (x == frameCoordinates[coordinateIndex++] && y == frameCoordinates[coordinateIndex++]) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Locates an edge of a triangle which contains a location
   * specified by a PointDoubleXYZ v.
   * The edge returned has the
   * property that either v is on e, or e is an edge of a triangle containing v.
   * The search starts from startEdge amd proceeds on the general direction of v.
   * <p>
   * This locate algorithm relies on the subdivision being Delaunay. For
   * non-Delaunay subdivisions, this may loop for ever.
   *
   * @param vertex the location to search for
   * @param startEdge an edge of the subdivision to start searching at
   * @returns a QuadEdge which contains v, or is on the edge of a triangle containing v
   * @throws LocateFailureException
   *           if the location algorithm fails to converge in a reasonable
   *           number of iterations
   */

  public QuadEdge locate(final double x, final double y) {
    QuadEdge currentEdge = this.lastEdge;

    final int maxIterations = this.quadEdges.size();
    for (int interationCount = 1; interationCount < maxIterations; interationCount++) {
      final double x1 = currentEdge.getX(0);
      final double y1 = currentEdge.getY(0);
      if (x == x1 && y == y1) {
        this.lastEdge = currentEdge;
        return currentEdge;
      } else {
        final double x2 = currentEdge.getX(1);
        final double y2 = currentEdge.getY(1);
        if (x == x2 && y == y2) {
          this.lastEdge = currentEdge;
          return currentEdge;
        } else if (Side.getSide(x1, y1, x2, y2, x, y) == Side.RIGHT) {
          currentEdge = currentEdge.sym();
        } else {
          final QuadEdge fromNextEdge = currentEdge.getFromNextEdge();
          final double fromNextEdgeX2 = fromNextEdge.getX(1);
          final double fromNextEdgeY2 = fromNextEdge.getY(1);
          if (Side.getSide(x1, y1, fromNextEdgeX2, fromNextEdgeY2, x, y) == Side.LEFT) {
            currentEdge = fromNextEdge;
          } else {
            final QuadEdge toNextEdge = currentEdge.getToNextEdge();
            final double toNextEdgeX1 = toNextEdge.getX(0);
            final double toNextEdgeY1 = toNextEdge.getY(0);

            if (Side.getSide(toNextEdgeX1, toNextEdgeY1, x2, y2, x, y) == Side.LEFT) {
              currentEdge = toNextEdge;
            } else {
              this.lastEdge = currentEdge; // on edge or in triangle containing edge
              return currentEdge;
            }
          }
        }
      }
    }
    throw new LocateFailureException(currentEdge);
  }

  /**
   * Creates a new quadedge, recording it in the edges list.
   *
   * @param o
   * @param d
   * @return a new quadedge
   */
  public QuadEdge makeEdge(final Point o, final Point d) {
    final QuadEdge q = QuadEdge.makeEdge(o, d);
    this.quadEdges.add(q);
    return q;
  }

  private void newFrame(final double[] bounds) {
    final double minX = bounds[0];
    final double minY = bounds[1];
    final double maxX = bounds[2];
    final double maxY = bounds[3];
    final double width = maxX - minX;
    final double height = maxY - minY;
    double offset = 0.0;
    if (width > height) {
      offset = width * 10.0;
    } else {
      offset = height * 10.0;
    }

    final double x1 = this.geometryFactory.makeXyPrecise(minX + width / 2.0);
    final double y1 = this.geometryFactory.makeXyPrecise(maxY + offset);
    this.frameVertex[0] = new PointDoubleXY(x1, y1);
    final double x2 = this.geometryFactory.makeXyPrecise(minX - offset);
    final double y2 = this.geometryFactory.makeXyPrecise(minY - offset);
    this.frameVertex[1] = new PointDoubleXY(x2, y2);
    final double x3 = this.geometryFactory.makeXyPrecise(maxX + offset);
    this.frameVertex[2] = new PointDoubleXY(x3, y2);
    this.frameCoordinates = new double[] {
      x1, y1, x2, y2, x3, y2
    };
    this.frameBoundingBox = BoundingBoxDoubleXY.newBoundingBox(this.frameVertex[0],
      this.frameVertex[1], this.frameVertex[2]);
  }

  public void resetLastEdge() {
    if (!this.lastEdge.isLive()) {
      initLocator();
    }
  }
}
