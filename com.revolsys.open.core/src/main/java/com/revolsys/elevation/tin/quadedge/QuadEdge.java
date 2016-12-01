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

package com.revolsys.elevation.tin.quadedge;

import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Side;
import com.revolsys.geometry.model.segment.LineSegment;
import com.revolsys.geometry.model.segment.LineSegmentDoubleGF;

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
 * The edge class does not contain separate information for vertices or faces; a PointDoubleXYZ is implicitly
 * defined as a ring of edges (created using the <tt>next</tt> field).
 *
 * @author David Skea
 * @author Martin Davis
 */
public class QuadEdge {

  /**
   * Splices two edges together or apart.
   * Splice affects the two edge rings around the origins of a and b, and, independently, the two
   * edge rings around the left faces of <tt>a</tt> and <tt>b</tt>.
   * In each case, (i) if the two rings are distinct,
   * Splice will combine them into one, or (ii) if the two are the same ring, Splice will break it
   * into two separate pieces. Thus, Splice can be used both to attach the two edges together, and
   * to break them apart.
   *
   * @param edge1 an edge to splice
   * @param edge2 an edge to splice
   *
   */
  public static void splice(final QuadEdge edge1, final QuadEdge edge2) {
    final QuadEdge fromNextEdge1 = edge1.getFromNextEdge();
    final QuadEdge fromNextEdge2 = edge2.getFromNextEdge();

    final QuadEdge alpha = fromNextEdge1.rot();
    final QuadEdge beta = fromNextEdge2.rot();

    final QuadEdge fromNextEdgeRot1 = beta.getFromNextEdge();
    final QuadEdge fromNextEdgeRot2 = alpha.getFromNextEdge();

    edge1.setNext(fromNextEdge2);
    edge2.setNext(fromNextEdge1);
    alpha.setNext(fromNextEdgeRot1);
    beta.setNext(fromNextEdgeRot2);
  }

  /**
   * Turns an edge counterclockwise inside its enclosing quadrilateral.
   *
   * @param edge the quadedge to turn
   */
  public static void swap(final QuadEdge edge) {
    final QuadEdge edgePrevious = edge.oPrev();
    final QuadEdge b = edge.sym().oPrev();
    splice(edge, edgePrevious);
    splice(edge.sym(), b);
    splice(edge, edgePrevious.lNext());
    splice(edge.sym(), b.lNext());
    edge.setFromPoint(edgePrevious.getToPoint());
    edge.setDest(b.getToPoint());
  }

  private Object data = null;

  private QuadEdge next; // A reference to a connected edge

  // the dual of this edge, directed from right to left
  private QuadEdge rot;

  private Point fromPoint; // The Point that this edge
                           // represents

  /**
   * Quadedges must be made using {@link makeEdge},
   * to ensure proper construction.
   */
  QuadEdge() {

  }

  QuadEdge(final Point fromPoint) {
    this.fromPoint = fromPoint;
    this.rot = new QuadEdge();
    this.next = this;
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

  /***************************************************************************
   * QuadEdge Algebra
   ***************************************************************************
   */

  @Override
  public boolean equals(final Object other) {
    return other == this;
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

  public boolean equalsVertex(final int vertexIndex, final double x, final double y) {
    if (vertexIndex == 0) {
      return this.fromPoint.equalsVertex(x, y);
    } else if (vertexIndex == 0) {
      final Point toPoint = getToPoint();
      return toPoint.equalsVertex(x, y);
    } else {
      return false;
    }
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
   * Gets the Point for the edge's origin
   *
   * @return the origin Point
   */

  public final Point getFromPoint() {
    return this.fromPoint;
  }

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

  public Side getSide(final double x, final double y) {
    final double x1 = this.fromPoint.getX();
    final double y1 = this.fromPoint.getY();
    final Point toPoint = getToPoint();
    final double x2 = toPoint.getX();
    final double y2 = toPoint.getY();
    return Side.getSide(x1, y1, x2, y2, x, y);
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
   * Gets the Point for the edge's destination
   *
   * @return the destination Point
   */

  public final Point getToPoint() {
    return sym().getFromPoint();
  }

  public double getX(final int vertexIndex) {
    if (vertexIndex == 0) {
      return this.fromPoint.getX();
    } else if (vertexIndex == 1) {
      return getToPoint().getX();
    } else {
      return Double.NaN;
    }
  }

  public double getY(final int vertexIndex) {
    if (vertexIndex == 0) {
      return this.fromPoint.getY();
    } else if (vertexIndex == 1) {
      return getToPoint().getY();
    } else {
      return Double.NaN;
    }
  }

  void init(final QuadEdge rot, final QuadEdge next) {
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

  public boolean isInCircle(final double x2, final double y2, final double x, final double y) {
    final Point point1 = this.fromPoint;
    final double x1 = point1.getX();
    final double y1 = point1.getY();
    final Point point3 = getToPoint();
    final double x3 = point3.getX();
    final double y3 = point3.getY();

    final double deltaX1 = x1 - x;
    final double deltaY1 = y1 - y;
    final double deltaX2 = x2 - x;
    final double deltaY2 = y2 - y;
    final double deltaX3 = x3 - x;
    final double deltaY3 = y3 - y;

    final double abdet = deltaX1 * deltaY2 - deltaX2 * deltaY1;
    final double bcdet = deltaX2 * deltaY3 - deltaX3 * deltaY2;
    final double cadet = deltaX3 * deltaY1 - deltaX1 * deltaY3;
    final double alift = deltaX1 * deltaX1 + deltaY1 * deltaY1;
    final double blift = deltaX2 * deltaX2 + deltaY2 * deltaY2;
    final double clift = deltaX3 * deltaX3 + deltaY3 * deltaY3;

    final double disc = alift * bcdet + blift * cadet + clift * abdet;
    return disc > 0;
    // return Triangles.isInCircleNormalized(x1, y1, x2, y2, x3, y3, x, y);
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

  public LineSegment newLineString(final GeometryFactory geometryFactory) {
    final Point toPoint = getToPoint();
    return new LineSegmentDoubleGF(geometryFactory, this.fromPoint, toPoint);
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
   * Sets the Point for this edge's destination
   *
   * @param d the destination Point
   */
  void setDest(final Point d) {
    sym().setFromPoint(d);
  }

  /***********************************************************************************************
   * Data Access
   **********************************************************************************************/
  /**
   * Sets the Point for this edge's origin
   *
   * @param fromPoint the origin Point
   */
  void setFromPoint(final Point fromPoint) {
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

  @Override
  public String toString() {
    return newLineString(GeometryFactory.DEFAULT_3D).toString();
  }
}
