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

import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.AbstractLineString;
import com.revolsys.geometry.model.segment.LineSegment;

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
 * The edge class does not contain separate information for vertices or faces; a quadEdgeVertex is implicitly
 * defined as a ring of edges (created using the <tt>next</tt> field).
 *
 * @author David Skea
 * @author Martin Davis
 */
public class QuadEdge extends AbstractLineString implements LineSegment {
  /**
   * Creates a new QuadEdge connecting the destination of a to the origin of
   * b, in such a way that all three have the same left face after the
   * connection is complete. Additionally, the data pointers of the new edge
   * are set.
   *
   * @return the connected edge.
   */
  public static QuadEdge connect(final QuadEdge a, final QuadEdge b) {
    final QuadEdge e = makeEdge(a.getToPoint(), b.getFromPoint());
    splice(e, a.lNext());
    splice(e.sym(), b);
    return e;
  }

  /**
   * Creates a new QuadEdge quartet from {@link QuadEdgeVertex} o to {@link QuadEdgeVertex} d.
   *
   * @param fromPoint
   *          the origin QuadEdgeVertex
   * @param toPoint
   *          the destination QuadEdgeVertex
   * @return the new QuadEdge quartet
   */
  public static QuadEdge makeEdge(final QuadEdgeVertex fromPoint, final QuadEdgeVertex toPoint) {
    final QuadEdge base = new QuadEdge(fromPoint);
    final QuadEdge q1 = new QuadEdge();
    final QuadEdge q2 = new QuadEdge(toPoint);
    final QuadEdge q3 = new QuadEdge();

    base.init(q1, base);
    q1.init(q2, q3);
    q2.init(q3, q2);
    q3.init(base, q1);
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
    final QuadEdge alpha = a.getFromNextEdge().rot();
    final QuadEdge beta = b.getFromNextEdge().rot();

    final QuadEdge t1 = b.getFromNextEdge();
    final QuadEdge t2 = a.getFromNextEdge();
    final QuadEdge t3 = beta.getFromNextEdge();
    final QuadEdge t4 = alpha.getFromNextEdge();

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
    e.setFromPoint(a.getToPoint());
    e.setDest(b.getToPoint());
  }

  private Object data = null;

  private QuadEdge next; // A reference to a connected edge

  // the dual of this edge, directed from right to left
  private QuadEdge rot;

  private QuadEdgeVertex fromPoint; // The quadEdgeVertex that this edge
                                    // represents

  /**
   * Quadedges must be made using {@link makeEdge},
   * to ensure proper construction.
   */
  private QuadEdge() {

  }

  private QuadEdge(final QuadEdgeVertex fromPoint) {
    this.fromPoint = fromPoint;
  }

  @Override
  public LineString clone() {
    return this;
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
    this.rot = null;
  }

  /**
   * Gets the next CCW edge around (into) the destination of this edge.
   *
   * @return the next destination edge.
   */
  public final QuadEdge dNext() {
    return this.sym().getFromNextEdge().sym();
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
    if (this.fromPoint.equals(2, qe.getFromPoint()) && getToPoint().equals(2, qe.getToPoint())) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public double getCoordinate(final int vertexIndex, final int axisIndex) {
    if (vertexIndex == 0) {
      return this.fromPoint.getCoordinate(axisIndex);
    } else if (vertexIndex == 1) {
      return getToPoint().getCoordinate(axisIndex);
    } else {
      return Double.NaN;
    }
  }

  /***************************************************************************
   * QuadEdge Algebra
   ***************************************************************************
   */

  @Override
  public double[] getCoordinates() {
    final double[] coordinates = {
      getX(0), getY(0), getZ(0), getX(1), getY(1), getZ(1)
    };
    return coordinates;
  }

  /**
   * Gets the external data value for this edge.
   *
   * @return the data object
   */
  public Object getData() {
    return this.data;
  }

  /**
   * Gets the next CCW edge around the origin of this edge.
   *
   * @return the next linked edge.
   */
  public final QuadEdge getFromNextEdge() {
    return this.next;
  }

  /**
   * Gets the quadEdgeVertex for the edge's origin
   *
   * @return the origin quadEdgeVertex
   */
  @Override
  public final QuadEdgeVertex getFromPoint() {
    return this.fromPoint;
  }

  @Override
  public Point getPoint(final int vertexIndex) {
    if (vertexIndex == 0) {
      return this.fromPoint;
    } else if (vertexIndex == 1) {
      return getToPoint();
    } else {
      throw new ArrayIndexOutOfBoundsException(vertexIndex);
    }
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
    if (this.fromPoint.compareTo(getToPoint()) <= 0) {
      return this;
    } else {
      return sym();
    }
  }

  /**
   * Gets the next CW edge around (into) the destination of this edge.
   *
   * @return the previous destination edge.
   */
  public final QuadEdge getToNextEdge() {
    return this.invRot().getFromNextEdge().invRot();
  }

  /**
   * Gets the quadEdgeVertex for the edge's destination
   *
   * @return the destination quadEdgeVertex
   */
  @Override
  public final QuadEdgeVertex getToPoint() {
    return sym().getFromPoint();
  }

  @Override
  public int getVertexCount() {
    return 2;
  }

  @Override
  public double getX(final int vertexIndex) {
    if (vertexIndex == 0) {
      return this.fromPoint.getX();
    } else if (vertexIndex == 1) {
      return getToPoint().getX();
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double getY(final int vertexIndex) {
    if (vertexIndex == 0) {
      return this.fromPoint.getY();
    } else if (vertexIndex == 1) {
      return getToPoint().getY();
    } else {
      return Double.NaN;
    }
  }

  @Override
  public double getZ(final int vertexIndex) {
    if (vertexIndex == 0) {
      return this.fromPoint.getZ();
    } else if (vertexIndex == 1) {
      return getToPoint().getZ();
    } else {
      return Double.NaN;
    }
  }

  private void init(final QuadEdge rot, final QuadEdge next) {
    this.rot = rot;
    this.next = next;
  }

  /**
   * Gets the dual of this edge, directed from its left to its right.
   *
   * @return the inverse rotated edge.
   */
  public final QuadEdge invRot() {
    return this.rot.sym();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  /**
   * Tests whether this edge has been deleted.
   *
   * @return true if this edge has not been deleted.
   */
  public boolean isLive() {
    return this.rot != null;
  }

  /**
   * Gets the CCW edge around the left face following this edge.
   *
   * @return the next left face edge.
   */
  public final QuadEdge lNext() {
    return this.invRot().getFromNextEdge().rot();
  }

  /**
   * Gets the CCW edge around the left face before this edge.
   *
   * @return the previous left face edge.
   */
  public final QuadEdge lPrev() {
    return this.next.sym();
  }

  /**
   * Gets the next CW edge around (from) the origin of this edge.
   *
   * @return the previous edge.
   */
  public final QuadEdge oPrev() {
    return this.rot.next.rot;
  }

  /**
   * Gets the edge around the right face ccw following this edge.
   *
   * @return the next right face edge.
   */
  public final QuadEdge rNext() {
    return this.rot.next.invRot();
  }

  /**
   * Gets the dual of this edge, directed from its right to its left.
   *
   * @return the rotated edge
   */
  public final QuadEdge rot() {
    return this.rot;
  }

  /**
   * Gets the edge around the right face ccw before this edge.
   *
   * @return the previous right face edge.
   */
  public final QuadEdge rPrev() {
    return this.sym().getFromNextEdge();
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
   * Sets the quadEdgeVertex for this edge's destination
   *
   * @param d the destination quadEdgeVertex
   */
  void setDest(final QuadEdgeVertex d) {
    sym().setFromPoint(d);
  }

  /***********************************************************************************************
   * Data Access
   **********************************************************************************************/
  /**
   * Sets the quadEdgeVertex for this edge's origin
   *
   * @param fromPoint the origin quadEdgeVertex
   */
  void setFromPoint(final QuadEdgeVertex fromPoint) {
    this.fromPoint = fromPoint;
  }

  /**
   * Sets the connected edge
   *
   * @param next edge
   */
  public void setNext(final QuadEdge next) {
    this.next = next;
  }

  /**
   * Gets the edge from the destination to the origin of this edge.
   *
   * @return the sym of the edge
   */
  public final QuadEdge sym() {
    return this.rot.rot;
  }
}
