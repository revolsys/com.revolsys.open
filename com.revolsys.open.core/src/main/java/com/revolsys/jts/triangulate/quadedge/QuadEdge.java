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

package com.revolsys.jts.triangulate.quadedge;

import com.revolsys.io.wkt.WktWriter;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.LineSegmentImpl;

/**
 * A class that represents the edge data structure which implements the quadedge algebra. 
 * The quadedge algebra was described in a well-known paper by Guibas and Stolfi,
 * "Primitives for the manipulation of general subdivisions and the computation of Voronoi diagrams", 
 * <i>ACM Transactions on Graphics</i>, 4(2), 1985, 75-123.
 * <p>
 * Each edge object is part of a quartet of 4 edges,
 * linked via their <tt>rot</tt> references.
 * Any edge in the group may be accessed using a series of {@link #rot()} operations.
 * Quadedges in a subdivision are linked together via their <tt>next</tt> references.
 * The linkage between the quadedge quartets determines the topology
 * of the subdivision. 
 * <p>
 * The edge class does not contain separate information for vertice or faces; a vertex is implicitly
 * defined as a ring of edges (created using the <tt>next</tt> field).
 * 
 * @author David Skea
 * @author Martin Davis
 */
public class QuadEdge {
  /**
   * Creates a new QuadEdge connecting the destination of a to the origin of
   * b, in such a way that all three have the same left face after the
   * connection is complete. Additionally, the data pointers of the new edge
   * are set.
   * 
   * @return the connected edge.
   */
  public static QuadEdge connect(final QuadEdge a, final QuadEdge b) {
    final QuadEdge e = makeEdge(a.dest(), b.orig());
    splice(e, a.lNext());
    splice(e.sym(), b);
    return e;
  }

  /**
   * Creates a new QuadEdge quartet from {@link Vertex} o to {@link Vertex} d.
   * 
   * @param o
   *          the origin Vertex
   * @param d
   *          the destination Vertex
   * @return the new QuadEdge quartet
   */
  public static QuadEdge makeEdge(final Vertex o, final Vertex d) {
    final QuadEdge q0 = new QuadEdge();
    final QuadEdge q1 = new QuadEdge();
    final QuadEdge q2 = new QuadEdge();
    final QuadEdge q3 = new QuadEdge();

    q0.rot = q1;
    q1.rot = q2;
    q2.rot = q3;
    q3.rot = q0;

    q0.setNext(q0);
    q1.setNext(q3);
    q2.setNext(q2);
    q3.setNext(q1);

    final QuadEdge base = q0;
    base.setOrig(o);
    base.setDest(d);
    return base;
  }

  /**
   * Splices two edges together or apart.
   * Splice affects the two edge rings around the origins of a and b, and, independently, the two
   * edge rings around the left faces of <tt>a</tt> and <tt>b</tt>. 
   * In each case, (i) if the two rings are distinct,
   * Splice will combine them into one, or (ii) if the two are the same ring, Splice will break it
   * into two separate pieces. Thus, Splice can be used both to attach the two edges together, and
   * to break them apart.
   * 
   * @param a an edge to splice
   * @param b an edge to splice
   * 
   */
  public static void splice(final QuadEdge a, final QuadEdge b) {
    final QuadEdge alpha = a.oNext().rot();
    final QuadEdge beta = b.oNext().rot();

    final QuadEdge t1 = b.oNext();
    final QuadEdge t2 = a.oNext();
    final QuadEdge t3 = beta.oNext();
    final QuadEdge t4 = alpha.oNext();

    a.setNext(t1);
    b.setNext(t2);
    alpha.setNext(t3);
    beta.setNext(t4);
  }

  /**
   * Turns an edge counterclockwise inside its enclosing quadrilateral.
   * 
   * @param e the quadedge to turn
   */
  public static void swap(final QuadEdge e) {
    final QuadEdge a = e.oPrev();
    final QuadEdge b = e.sym().oPrev();
    splice(e, a);
    splice(e.sym(), b);
    splice(e, a.lNext());
    splice(e.sym(), b.lNext());
    e.setOrig(a.dest());
    e.setDest(b.dest());
  }

  // the dual of this edge, directed from right to left
  private QuadEdge rot;

  private Vertex vertex; // The vertex that this edge represents

  private QuadEdge next; // A reference to a connected edge

  private Object data = null;

  // private int visitedKey = 0;

  /**
   * Quadedges must be made using {@link makeEdge}, 
   * to ensure proper construction.
   */
  private QuadEdge() {

  }

  /**
   * Marks this quadedge as being deleted.
   * This does not free the memory used by
   * this quadedge quartet, but indicates
   * that this edge no longer participates
   * in a subdivision.
   *
   */
  public void delete() {
    rot = null;
  }

  /**
   * Gets the vertex for the edge's destination
   * 
   * @return the destination vertex
   */
  public final Vertex dest() {
    return sym().orig();
  }

  /**
   * Gets the next CCW edge around (into) the destination of this edge.
   * 
   * @return the next destination edge.
   */
  public final QuadEdge dNext() {
    return this.sym().oNext().sym();
  }

  /**
   * Gets the next CW edge around (into) the destination of this edge.
   * 
   * @return the previous destination edge.
   */
  public final QuadEdge dPrev() {
    return this.invRot().oNext().invRot();
  }

  /**
   * Tests if this quadedge and another have the same line segment geometry, 
   * regardless of orientation.
   * 
   * @param qe a quadege
   * @return true if the quadedges are based on the same line segment regardless of orientation
   */
  public boolean equalsNonOriented(final QuadEdge qe) {
    if (equalsOriented(qe)) {
      return true;
    }
    if (equalsOriented(qe.sym())) {
      return true;
    }
    return false;
  }

  /**
   * Tests if this quadedge and another have the same line segment geometry
   * with the same orientation.
   * 
   * @param qe a quadege
   * @return true if the quadedges are based on the same line segment
   */
  public boolean equalsOriented(final QuadEdge qe) {
    if (orig().getCoordinate().equals2d(qe.orig().getCoordinate())
      && dest().getCoordinate().equals2d(qe.dest().getCoordinate())) {
      return true;
    }
    return false;
  }

  /***************************************************************************
   * QuadEdge Algebra 
   ***************************************************************************
   */

  /**
   * Gets the external data value for this edge.
   * 
   * @return the data object
   */
  public Object getData() {
    return data;
  }

  /**
   * Gets the length of the geometry of this quadedge.
   * 
   * @return the length of the quadedge
   */
  public double getLength() {
    return orig().getCoordinate().distance(dest().getCoordinate());
  }

  /**
   * Gets the primary edge of this quadedge and its <tt>sym</tt>.
   * The primary edge is the one for which the origin
   * and destination coordinates are ordered
   * according to the standard {@link Coordinates} ordering
   * 
   * @return the primary quadedge
   */
  public QuadEdge getPrimary() {
    if (orig().getCoordinate().compareTo(dest().getCoordinate()) <= 0) {
      return this;
    } else {
      return sym();
    }
  }

  /**
   * Gets the dual of this edge, directed from its left to its right.
   * 
   * @return the inverse rotated edge.
   */
  public final QuadEdge invRot() {
    return rot.sym();
  }

  /**
   * Tests whether this edge has been deleted.
   * 
   * @return true if this edge has not been deleted.
   */
  public boolean isLive() {
    return rot != null;
  }

  /**
   * Gets the CCW edge around the left face following this edge.
   * 
   * @return the next left face edge.
   */
  public final QuadEdge lNext() {
    return this.invRot().oNext().rot();
  }

  /**
   * Gets the CCW edge around the left face before this edge.
   * 
   * @return the previous left face edge.
   */
  public final QuadEdge lPrev() {
    return next.sym();
  }

  /**
   * Gets the next CCW edge around the origin of this edge.
   * 
   * @return the next linked edge.
   */
  public final QuadEdge oNext() {
    return next;
  }

  /**
   * Gets the next CW edge around (from) the origin of this edge.
   * 
   * @return the previous edge.
   */
  public final QuadEdge oPrev() {
    return rot.next.rot;
  }

  /**
   * Gets the vertex for the edge's origin
   * 
   * @return the origin vertex
   */
  public final Vertex orig() {
    return vertex;
  }

  /**
   * Gets the edge around the right face ccw following this edge.
   * 
   * @return the next right face edge.
   */
  public final QuadEdge rNext() {
    return rot.next.invRot();
  }

  /**
   * Gets the dual of this edge, directed from its right to its left.
   * 
   * @return the rotated edge
   */
  public final QuadEdge rot() {
    return rot;
  }

  /**
   * Gets the edge around the right face ccw before this edge.
   * 
   * @return the previous right face edge.
   */
  public final QuadEdge rPrev() {
    return this.sym().oNext();
  }

  /**
   * Sets the external data value for this edge.
   * 
   * @param data an object containing external data
   */
  public void setData(final Object data) {
    this.data = data;
  }

  /**
   * Sets the vertex for this edge's destination
   * 
   * @param d the destination vertex
   */
  void setDest(final Vertex d) {
    sym().setOrig(d);
  }

  /**
   * Sets the connected edge
   * 
   * @param next edge
   */
  public void setNext(final QuadEdge next) {
    this.next = next;
  }

  /***********************************************************************************************
   * Data Access
   **********************************************************************************************/
  /**
   * Sets the vertex for this edge's origin
   * 
   * @param o the origin vertex
   */
  void setOrig(final Vertex o) {
    vertex = o;
  }

  /**
   * Gets the edge from the destination to the origin of this edge.
   * 
   * @return the sym of the edge
   */
  public final QuadEdge sym() {
    return rot.rot;
  }

  /**
   * Creates a {@link LineSegmentImpl} representing the
   * geometry of this edge.
   * 
   * @return a LineSegmentImpl
   */
  public LineSegment toLineSegment() {
    return new LineSegmentImpl(vertex.getCoordinate(), dest().getCoordinate());
  }

  /**
   * Converts this edge to a WKT two-point <tt>LINESTRING</tt> indicating 
   * the geometry of this edge.
   * 
   * @return a String representing this edge's geometry
   */
  @Override
  public String toString() {
    final Point p0 = vertex.getCoordinate();
    final Point p1 = dest().getCoordinate();
    return WktWriter.lineString(p0, p1);
  }
}
