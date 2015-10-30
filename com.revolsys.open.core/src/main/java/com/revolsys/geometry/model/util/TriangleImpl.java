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
package com.revolsys.geometry.model.util;

import com.revolsys.geometry.algorithm.CGAlgorithms;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.util.Triangles;

/**
 * Represents a planar triangle, and provides methods for calculating various
 * properties of triangles.
 *
 * @version 1.7
 */
public class TriangleImpl {

  /**
   * Computes the circumcentre of a triangle. The circumcentre is the centre of
   * the circumcircle, the smallest circle which encloses the triangle. It is
   * also the common intersection point of the perpendicular bisectors of the
   * sides of the triangle, and is the only point which has equal distance to
   * all three vertices of the triangle.
   *
   * @param a
   *          a vertx of the triangle
   * @param b
   *          a vertx of the triangle
   * @param c
   *          a vertx of the triangle
   * @return the circumcentre of the triangle
   */
  /*
   * // original non-robust algorithm public static Coordinate
   * circumcentre(Point a, Point b, Point c) { // compute the perpendicular
   * bisector of chord ab HCoordinate cab = perpendicularBisector(a, b); //
   * compute the perpendicular bisector of chord bc HCoordinate cbc =
   * perpendicularBisector(b, c); // compute the intersection of the bisectors
   * (circle radii) HCoordinate hcc = new HCoordinate(cab, cbc); Point cc =
   * null; try { cc = new Coordinate(hcc.getX(), hcc.getY()); } catch
   * (NotRepresentableException ex) { // MD - not sure what we can do to prevent
   * this (robustness problem) // Idea - can we condition which edges we choose?
   * throw new IllegalStateException(ex.getMessage()); } //System.out.println(
   * "Acc = " + a.distance(cc) + ", Bcc = " + b.distance(cc) + ", Ccc = " +
   * c.distance(cc) ); return cc; }
   */

  /**
   * The coordinates of the vertices of the triangle
   */
  public Point p0, p1, p2;

  /**
   * Creates a new triangle with the given vertices.
   *
   * @param p0
   *          a vertex
   * @param p1
   *          a vertex
   * @param p2
   *          a vertex
   */
  public TriangleImpl(final Point p0, final Point p1, final Point p2) {
    this.p0 = p0;
    this.p1 = p1;
    this.p2 = p2;
  }

  /**
   * Computes the 2D area of this triangle. The area value is always
   * non-negative.
   *
   * @return the area of this triangle
   *
   * @see #signedArea()
   */
  public double area() {
    return Triangles.area(this.p0, this.p1, this.p2);
  }

  /**
   * Computes the 3D area of this triangle. The value computed is alway
   * non-negative.
   *
   * @return the 3D area of this triangle
   */
  public double area3D() {
    return Triangles.area3D(this.p0, this.p1, this.p2);
  }

  /**
   * Computes the centroid (centre of mass) of this triangle. This is also the
   * point at which the triangle's three medians intersect (a triangle median is
   * the segment from a vertex of the triangle to the midpoint of the opposite
   * side). The centroid divides each median in a ratio of 2:1.
   * <p>
   * The centroid always lies within the triangle.
   *
   * @return the centroid of this triangle
   */
  public Point centroid() {
    return Triangles.centroid(this.p0, this.p1, this.p2);
  }

  /**
   * Computes the circumcentre of this triangle. The circumcentre is the centre
   * of the circumcircle, the smallest circle which encloses the triangle. It is
   * also the common intersection point of the perpendicular bisectors of the
   * sides of the triangle, and is the only point which has equal distance to
   * all three vertices of the triangle.
   * <p>
   * The circumcentre does not necessarily lie within the triangle.
   * <p>
   * This method uses an algorithm due to J.R.Shewchuk which uses normalization
   * to the origin to improve the accuracy of computation. (See <i>Lecture Notes
   * on Geometric Robustness</i>, Jonathan Richard Shewchuk, 1999).
   *
   * @return the circumcentre of this triangle
   */
  public Point circumcentre() {
    return Triangles.circumcentre(this.p0, this.p1, this.p2);
  }

  /**
   * Computes the incentre of this triangle. The <i>incentre</i> of a triangle
   * is the point which is equidistant from the sides of the triangle. It is
   * also the point at which the bisectors of the triangle's angles meet. It is
   * the centre of the triangle's <i>incircle</i>, which is the unique circle
   * that is tangent to each of the triangle's three sides.
   *
   * @return the point which is the inCentre of this triangle
   */
  public Point inCentre() {
    return Triangles.inCentre(this.p0, this.p1, this.p2);
  }

  /**
   * Computes the Z-value (elevation) of an XY point on a three-dimensional
   * plane defined by this triangle (whose vertices must have Z-values). This
   * triangle must not be degenerate (in other words, the triangle must enclose
   * a non-zero area), and must not be parallel to the Z-axis.
   * <p>
   * This method can be used to interpolate the Z-value of a point inside this
   * triangle (for example, of a TIN facet with elevations on the vertices).
   *
   * @param p
   *          the point to compute the Z-value of
   * @return the computed Z-value (elevation) of the point
   */
  public double interpolateZ(final Point p) {
    if (p == null) {
      throw new IllegalArgumentException("Supplied point is null.");
    }
    return Triangles.interpolateZ(p, this.p0, this.p1, this.p2);
  }

  /**
   * Tests whether this triangle is acute. A triangle is acute iff all interior
   * angles are acute. This is a strict test - right triangles will return
   * <tt>false</tt> A triangle which is not acute is either right or obtuse.
   * <p>
   * Note: this implementation is not robust for angles very close to 90
   * degrees.
   *
   * @return true if this triangle is acute
   */
  public boolean isAcute() {
    return Triangles.isAcute(this.p0, this.p1, this.p2);
  }

  /**
   * Computes the length of the longest side of this triangle
   *
   * @return the length of the longest side of this triangle
   */
  public double longestSideLength() {
    return Triangles.longestSideLength(this.p0, this.p1, this.p2);
  }

  /**
   * Computes the signed 2D area of this triangle. The area value is positive if
   * the triangle is oriented CW, and negative if it is oriented CCW.
   * <p>
   * The signed area value can be used to determine point orientation, but the
   * implementation in this method is susceptible to round-off errors. Use
   * {@link CGAlgorithms#orientationIndex(Coordinate, Coordinate, Coordinate)}
   * for robust orientation calculation.
   *
   * @return the signed 2D area of this triangle
   *
   * @see CGAlgorithms#orientationIndex(Coordinate, Coordinate, Coordinate)
   */
  public double signedArea() {
    return Triangles.signedArea(this.p0, this.p1, this.p2);
  }

}
