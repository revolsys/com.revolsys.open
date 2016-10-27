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
import java.util.List;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Polygon;

/**
 * Models a triangle formed from {@link QuadEdge}s in a {@link QuadEdgeSubdivision}
 * which forms a triangulation. The class provides methods to access the
 * topological and geometric properties of the triangle and its neighbours in
 * the triangulation. TriangleImpl vertices are ordered in CCW orientation in the
 * structure.
 * <p>
 * QuadEdgeTriangles support having an external data attribute attached to them.
 * Alternatively, this class can be subclassed and attributes can
 * be defined in the subclass.  Subclasses will need to define
 * their own <tt>BuilderVisitor</tt> class
 * and <tt>createOn</tt> method.
 *
 * @author Martin Davis
 * @version 1.0
 */
public class QuadEdgeTriangle {
  private static class QuadEdgeTriangleBuilderVisitor implements TriangleVisitor {
    private final List triangles = new ArrayList();

    public QuadEdgeTriangleBuilderVisitor() {
    }

    public List getTriangles() {
      return this.triangles;
    }

    @Override
    public void visit(final QuadEdge[] edges) {
      this.triangles.add(new QuadEdgeTriangle(edges));
    }
  }

  /**
   * Tests whether the point pt is contained in the triangle defined by 3
   * {@link QuadEdge}es.
   *
   * @param tri
   *          an array containing at least 3 QuadEdges
   * @param pt
   *          the point to test
   * @return true if the point is contained in the triangle
   */
  public static boolean contains(final QuadEdge[] tri, final Point pt) {
    final LineString ring = GeometryFactory.DEFAULT.lineString(tri[0].orig(), tri[1].orig(),
      tri[2].orig(), tri[0].orig());
    return ring.isPointInRing(pt);
  }

  /**
   * Tests whether the point pt is contained in the triangle defined by 3
   * {@link QuadEdgeVertex}es.
   *
   * @param tri
   *          an array containing at least 3 Vertexes
   * @param pt
   *          the point to test
   * @return true if the point is contained in the triangle
   */
  public static boolean contains(final QuadEdgeVertex[] tri, final Point pt) {
    final LineString ring = GeometryFactory.DEFAULT.lineString(tri[0], tri[1], tri[2], tri[0]);
    return ring.isPointInRing(pt);
  }

  /**
   * Creates {@link QuadEdgeTriangle}s for all facets of a
   * {@link QuadEdgeSubdivision} representing a triangulation.
   * The <tt>data</tt> attributes of the {@link QuadEdge}s in the subdivision
   * will be set to point to the triangle which contains that edge.
   * This allows tracing the neighbour triangles of any given triangle.
   *
   * @param subdiv
   * the QuadEdgeSubdivision to create the triangles on.
   * @return a List of the created QuadEdgeTriangles
   */
  public static List createOn(final QuadEdgeSubdivision subdiv) {
    final QuadEdgeTriangleBuilderVisitor visitor = new QuadEdgeTriangleBuilderVisitor();
    subdiv.visitTriangles(visitor, false);
    return visitor.getTriangles();
  }

  /**
   * Finds the next index around the triangle. Index may be an edge or vertex
   * index.
   *
   * @param index
   * @return the next index
   */
  public static int nextIndex(int index) {
    return index = (index + 1) % 3;
  }

  public static Geometry toPolygon(final QuadEdge[] e) {
    final Point[] ringPts = new Point[] {
      e[0].orig(), e[1].orig(), e[2].orig(), e[0].orig()
    };
    final GeometryFactory fact = GeometryFactory.DEFAULT;
    final LinearRing ring = fact.linearRing(ringPts);
    final Polygon tri = fact.polygon(ring);
    return tri;
  }

  public static Geometry toPolygon(final QuadEdgeVertex[] v) {
    final Point[] ringPts = new Point[] {
      v[0], v[1], v[2], v[0]
    };
    final GeometryFactory fact = GeometryFactory.DEFAULT;
    final LinearRing ring = fact.linearRing(ringPts);
    final Polygon tri = fact.polygon(ring);
    return tri;
  }

  private Object data;

  private QuadEdge[] edge;

  /**
   * Creates a new triangle from the given edges.
   *
   * @param edge an array of the edges of the triangle in CCW order
   */
  public QuadEdgeTriangle(final QuadEdge[] edge) {
    this.edge = edge.clone();
    // link the quadedges back to this triangle
    for (int i = 0; i < 3; i++) {
      edge[i].setData(this);
    }
  }

  public boolean contains(final Point pt) {
    return getLine().isPointInRing(pt);
  }

  public QuadEdgeTriangle getAdjacentTriangleAcrossEdge(final int edgeIndex) {
    return (QuadEdgeTriangle)getEdge(edgeIndex).sym().getData();
  }

  public int getAdjacentTriangleEdgeIndex(final int i) {
    return getAdjacentTriangleAcrossEdge(i).getEdgeIndex(getEdge(i).sym());
  }

  public Point getCoordinate(final int i) {
    return this.edge[i].orig();
  }

  public double[] getCoordinates() {
    final double[] coordinates = new double[12];
    int coordinateIndex = 0;
    final Point[] pts = new Point[4];
    for (int i = 0; i < 3; i++) {
      final QuadEdgeVertex point = this.edge[i].orig();
      coordinates[coordinateIndex++] = point.getX();
      coordinates[coordinateIndex++] = point.getY();
      coordinates[coordinateIndex++] = point.getZ();
      pts[i] = point;
    }
    coordinates[9] = coordinates[0];
    coordinates[10] = coordinates[1];
    coordinates[11] = coordinates[2];
    return coordinates;
  }

  /**
   * Gets the external data value for this triangle.
   *
   * @return the data object
   */
  public Object getData() {
    return this.data;
  }

  public QuadEdge getEdge(final int i) {
    return this.edge[i];
  }

  /**
   * Gets the index for the given edge of this triangle
   *
   * @param e
   *          a QuadEdge
   * @return the index of the edge in this triangle
   * or -1 if the edge is not an edge of this triangle
   */
  public int getEdgeIndex(final QuadEdge e) {
    for (int i = 0; i < 3; i++) {
      if (this.edge[i] == e) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Gets the index for the edge that starts at vertex v.
   *
   * @param v
   *          the vertex to find the edge for
   * @return the index of the edge starting at the vertex
   * or -1 if the vertex is not in the triangle
   */
  public int getEdgeIndex(final QuadEdgeVertex v) {
    for (int i = 0; i < 3; i++) {
      if (this.edge[i].orig() == v) {
        return i;
      }
    }
    return -1;
  }

  public QuadEdge[] getEdges() {
    return this.edge;
  }

  public Polygon getGeometry(final GeometryFactory fact) {
    final LinearRing ring = fact.linearRing(3, getCoordinates());
    final Polygon tri = fact.polygon(ring);
    return tri;
  }

  public LineString getLine() {
    return GeometryFactory.DEFAULT.lineString(3, getCoordinates());
  }

  /**
   * Gets the neighbours of this triangle. If there is no neighbour triangle,
   * the array element is <code>null</code>
   *
   * @return an array containing the 3 neighbours of this triangle
   */
  public QuadEdgeTriangle[] getNeighbours() {
    final QuadEdgeTriangle[] neigh = new QuadEdgeTriangle[3];
    for (int i = 0; i < 3; i++) {
      neigh[i] = (QuadEdgeTriangle)getEdge(i).sym().getData();
    }
    return neigh;
  }

  /**
   * Gets the triangles which are adjacent (include) to a
   * given vertex of this triangle.
   *
   * @param vertexIndex the vertex to query
   * @return a list of the vertex-adjacent triangles
   */
  public List getTrianglesAdjacentToVertex(final int vertexIndex) {
    // Assert: isVertex
    final List adjTris = new ArrayList();

    final QuadEdge start = getEdge(vertexIndex);
    QuadEdge qe = start;
    do {
      final QuadEdgeTriangle adjTri = (QuadEdgeTriangle)qe.getData();
      if (adjTri != null) {
        adjTris.add(adjTri);
      }
      qe = qe.oNext();
    } while (qe != start);

    return adjTris;

  }

  public QuadEdgeVertex getVertex(final int i) {
    return this.edge[i].orig();
  }

  /**
   * Gets the vertices for this triangle.
   *
   * @return a new array containing the triangle vertices
   */
  public QuadEdgeVertex[] getVertices() {
    final QuadEdgeVertex[] vert = new QuadEdgeVertex[3];
    for (int i = 0; i < 3; i++) {
      vert[i] = getVertex(i);
    }
    return vert;
  }

  /**
   * Tests whether this triangle is adjacent to the outside of the subdivision.
   *
   * @return true if the triangle is adjacent to the subdivision exterior
   */
  public boolean isBorder() {
    for (int i = 0; i < 3; i++) {
      if (getAdjacentTriangleAcrossEdge(i) == null) {
        return true;
      }
    }
    return false;
  }

  public boolean isBorder(final int i) {
    return getAdjacentTriangleAcrossEdge(i) == null;
  }

  public boolean isLive() {
    return this.edge != null;
  }

  public void kill() {
    this.edge = null;
  }

  /**
   * Sets the external data value for this triangle.
   *
   * @param data an object containing external data
   */
  public void setData(final Object data) {
    this.data = data;
  }

  @Override
  public String toString() {
    return getGeometry(GeometryFactory.DEFAULT).toString();
  }
}
