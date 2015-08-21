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

import com.revolsys.geometry.math.DD;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.Triangle;

/**
 * Algorithms for computing values and predicates
 * associated with triangles.
 * For some algorithms extended-precision
 * implementations are provided, which are more robust
 * (i.e. they produce correct answers in more cases).
 * Also, some more robust formulations of
 * some algorithms are provided, which utilize
 * normalization to the origin.
 *
 * @author Martin Davis
 *
 */
public class TrianglePredicate {
  /**
   * Computes the inCircle test using distance from the circumcentre.
   * Uses standard double-precision arithmetic.
   * <p>
   * In general this doesn't
   * appear to be any more robust than the standard calculation. However, there
   * is at least one case where the test point is far enough from the
   * circumcircle that this test gives the correct answer.
   * <pre>
   * LINESTRING
   * (1507029.9878 518325.7547, 1507022.1120341457 518332.8225183258,
   * 1507029.9833 518325.7458, 1507029.9896965567 518325.744909031)
   * </pre>
   *
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleCC(final Point a, final Point b, final Point c, final Point p) {
    final Point cc = Triangle.circumcentre(a, b, c);
    final double ccRadius = a.distance(cc);
    final double pRadiusDiff = p.distance(cc) - ccRadius;
    return pRadiusDiff <= 0;
  }

  public static boolean isInCircleDDFast(final Point a, final Point b, final Point c,
    final Point p) {
    final DD aTerm = DD.sqr(a.getX())
      .selfAdd(DD.sqr(a.getY()))
      .selfMultiply(triAreaDDFast(b, c, p));
    final DD bTerm = DD.sqr(b.getX())
      .selfAdd(DD.sqr(b.getY()))
      .selfMultiply(triAreaDDFast(a, c, p));
    final DD cTerm = DD.sqr(c.getX())
      .selfAdd(DD.sqr(c.getY()))
      .selfMultiply(triAreaDDFast(a, b, p));
    final DD pTerm = DD.sqr(p.getX())
      .selfAdd(DD.sqr(p.getY()))
      .selfMultiply(triAreaDDFast(a, b, c));

    final DD sum = aTerm.selfSubtract(bTerm).selfAdd(cTerm).selfSubtract(pTerm);
    final boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  public static boolean isInCircleDDNormalized(final Point a, final Point b, final Point c,
    final Point p) {
    final DD adx = DD.valueOf(a.getX()).selfSubtract(p.getX());
    final DD ady = DD.valueOf(a.getY()).selfSubtract(p.getY());
    final DD bdx = DD.valueOf(b.getX()).selfSubtract(p.getX());
    final DD bdy = DD.valueOf(b.getY()).selfSubtract(p.getY());
    final DD cdx = DD.valueOf(c.getX()).selfSubtract(p.getX());
    final DD cdy = DD.valueOf(c.getY()).selfSubtract(p.getY());

    final DD abdet = adx.multiply(bdy).selfSubtract(bdx.multiply(ady));
    final DD bcdet = bdx.multiply(cdy).selfSubtract(cdx.multiply(bdy));
    final DD cadet = cdx.multiply(ady).selfSubtract(adx.multiply(cdy));
    final DD alift = adx.multiply(adx).selfAdd(ady.multiply(ady));
    final DD blift = bdx.multiply(bdx).selfAdd(bdy.multiply(bdy));
    final DD clift = cdx.multiply(cdx).selfAdd(cdy.multiply(cdy));

    final DD sum = alift.selfMultiply(bcdet)
      .selfAdd(blift.selfMultiply(cadet))
      .selfAdd(clift.selfMultiply(abdet));

    final boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  /**
   * Tests if a point is inside the circle defined by
   * the triangle with vertices a, b, c (oriented counter-clockwise).
   * The computation uses {@link DD} arithmetic for robustness.
   *
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleDDSlow(final Point a, final Point b, final Point c,
    final Point p) {
    final DD px = DD.valueOf(p.getX());
    final DD py = DD.valueOf(p.getY());
    final DD ax = DD.valueOf(a.getX());
    final DD ay = DD.valueOf(a.getY());
    final DD bx = DD.valueOf(b.getX());
    final DD by = DD.valueOf(b.getY());
    final DD cx = DD.valueOf(c.getX());
    final DD cy = DD.valueOf(c.getY());

    final DD aTerm = ax.multiply(ax)
      .add(ay.multiply(ay))
      .multiply(triAreaDDSlow(bx, by, cx, cy, px, py));
    final DD bTerm = bx.multiply(bx)
      .add(by.multiply(by))
      .multiply(triAreaDDSlow(ax, ay, cx, cy, px, py));
    final DD cTerm = cx.multiply(cx)
      .add(cy.multiply(cy))
      .multiply(triAreaDDSlow(ax, ay, bx, by, px, py));
    final DD pTerm = px.multiply(px)
      .add(py.multiply(py))
      .multiply(triAreaDDSlow(ax, ay, bx, by, cx, cy));

    final DD sum = aTerm.subtract(bTerm).add(cTerm).subtract(pTerm);
    final boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  /**
   * Tests if a point is inside the circle defined by
   * the triangle with vertices a, b, c (oriented counter-clockwise).
   * This test uses simple
   * double-precision arithmetic, and thus may not be robust.
   *
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleNonRobust(final Point a, final Point b, final Point c,
    final Point p) {
    final boolean isInCircle = (a.getX() * a.getX() + a.getY() * a.getY()) * triArea(b, c, p)
      - (b.getX() * b.getX() + b.getY() * b.getY()) * triArea(a, c, p)
      + (c.getX() * c.getX() + c.getY() * c.getY()) * triArea(a, b, p)
      - (p.getX() * p.getX() + p.getY() * p.getY()) * triArea(a, b, c) > 0;
    return isInCircle;
  }

  /**
   * Tests if a point is inside the circle defined by
   * the triangle with vertices a, b, c (oriented counter-clockwise).
   * This test uses simple
   * double-precision arithmetic, and thus is not 100% robust.
   * However, by using normalization to the origin
   * it provides improved robustness and increased performance.
   * <p>
   * Based on code by J.R.Shewchuk.
   *
   *
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleNormalized(final Point a, final Point b, final Point c,
    final Point p) {
    final double adx = a.getX() - p.getX();
    final double ady = a.getY() - p.getY();
    final double bdx = b.getX() - p.getX();
    final double bdy = b.getY() - p.getY();
    final double cdx = c.getX() - p.getX();
    final double cdy = c.getY() - p.getY();

    final double abdet = adx * bdy - bdx * ady;
    final double bcdet = bdx * cdy - cdx * bdy;
    final double cadet = cdx * ady - adx * cdy;
    final double alift = adx * adx + ady * ady;
    final double blift = bdx * bdx + bdy * bdy;
    final double clift = cdx * cdx + cdy * cdy;

    final double disc = alift * bcdet + blift * cadet + clift * abdet;
    return disc > 0;
  }

  /**
   * Tests if a point is inside the circle defined by
   * the triangle with vertices a, b, c (oriented counter-clockwise).
   * This method uses more robust computation.
   *
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleRobust(final Point a, final Point b, final Point c,
    final Point p) {
    // checkRobustInCircle(a, b, c, p);
    // return isInCircleNonRobust(a, b, c, p);
    return isInCircleNormalized(a, b, c, p);
  }

  /**
   * Computes twice the area of the oriented triangle (a, b, c), i.e., the area is positive if the
   * triangle is oriented counterclockwise.
   *
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   */
  private static double triArea(final Point a, final Point b, final Point c) {
    return (b.getX() - a.getX()) * (c.getY() - a.getY())
      - (b.getY() - a.getY()) * (c.getX() - a.getX());
  }

  public static DD triAreaDDFast(final Point a, final Point b, final Point c) {

    final DD t1 = DD.valueOf(b.getX())
      .selfSubtract(a.getX())
      .selfMultiply(DD.valueOf(c.getY()).selfSubtract(a.getY()));

    final DD t2 = DD.valueOf(b.getY())
      .selfSubtract(a.getY())
      .selfMultiply(DD.valueOf(c.getX()).selfSubtract(a.getX()));

    return t1.selfSubtract(t2);
  }

  /**
   * Computes twice the area of the oriented triangle (a, b, c), i.e., the area
   * is positive if the triangle is oriented counterclockwise.
   * The computation uses {@link DD} arithmetic for robustness.
   *
   * @param ax the x ordinate of a vertex of the triangle
   * @param ay the y ordinate of a vertex of the triangle
   * @param bx the x ordinate of a vertex of the triangle
   * @param by the y ordinate of a vertex of the triangle
   * @param cx the x ordinate of a vertex of the triangle
   * @param cy the y ordinate of a vertex of the triangle
   */
  public static DD triAreaDDSlow(final DD ax, final DD ay, final DD bx, final DD by, final DD cx,
    final DD cy) {
    return bx.subtract(ax)
      .multiply(cy.subtract(ay))
      .subtract(by.subtract(ay).multiply(cx.subtract(ax)));
  }

}
