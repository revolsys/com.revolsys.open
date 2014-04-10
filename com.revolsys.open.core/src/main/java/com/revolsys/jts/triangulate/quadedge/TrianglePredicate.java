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

import java.util.Arrays;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Triangle;
import com.revolsys.jts.io.WKTWriter;
import com.revolsys.jts.math.DD;

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
   * Checks if the computed value for isInCircle is correct, using
   * double-double precision arithmetic.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   */
  private static void checkRobustInCircle(final Coordinate a,
    final Coordinate b, final Coordinate c, final Coordinate p) {
    final boolean nonRobustInCircle = isInCircleNonRobust(a, b, c, p);
    final boolean isInCircleDD = TrianglePredicate.isInCircleDDSlow(a, b, c, p);
    final boolean isInCircleCC = TrianglePredicate.isInCircleCC(a, b, c, p);

    final Coordinate circumCentre = Triangle.circumcentre(a, b, c);
    System.out.println("p radius diff a = "
      + Math.abs(p.distance(circumCentre) - a.distance(circumCentre))
      / a.distance(circumCentre));

    if (nonRobustInCircle != isInCircleDD || nonRobustInCircle != isInCircleCC) {
      System.out.println("inCircle robustness failure (double result = "
        + nonRobustInCircle + ", DD result = " + isInCircleDD
        + ", CC result = " + isInCircleCC + ")");
      System.out.println(Arrays.asList(a, b, c, p));
      System.out.println("Circumcentre = " + WKTWriter.toPoint(circumCentre)
        + " radius = " + a.distance(circumCentre));
      System.out.println("p radius diff a = "
        + Math.abs(p.distance(circumCentre) / a.distance(circumCentre) - 1));
      System.out.println("p radius diff b = "
        + Math.abs(p.distance(circumCentre) / b.distance(circumCentre) - 1));
      System.out.println("p radius diff c = "
        + Math.abs(p.distance(circumCentre) / c.distance(circumCentre) - 1));
      System.out.println();
    }
  }

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
  public static boolean isInCircleCC(final Coordinate a, final Coordinate b,
    final Coordinate c, final Coordinate p) {
    final Coordinate cc = Triangle.circumcentre(a, b, c);
    final double ccRadius = a.distance(cc);
    final double pRadiusDiff = p.distance(cc) - ccRadius;
    return pRadiusDiff <= 0;
  }

  public static boolean isInCircleDDFast(final Coordinate a,
    final Coordinate b, final Coordinate c, final Coordinate p) {
    final DD aTerm = (DD.sqr(a.x).selfAdd(DD.sqr(a.y))).selfMultiply(triAreaDDFast(
      b, c, p));
    final DD bTerm = (DD.sqr(b.x).selfAdd(DD.sqr(b.y))).selfMultiply(triAreaDDFast(
      a, c, p));
    final DD cTerm = (DD.sqr(c.x).selfAdd(DD.sqr(c.y))).selfMultiply(triAreaDDFast(
      a, b, p));
    final DD pTerm = (DD.sqr(p.x).selfAdd(DD.sqr(p.y))).selfMultiply(triAreaDDFast(
      a, b, c));

    final DD sum = aTerm.selfSubtract(bTerm).selfAdd(cTerm).selfSubtract(pTerm);
    final boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  public static boolean isInCircleDDNormalized(final Coordinate a,
    final Coordinate b, final Coordinate c, final Coordinate p) {
    final DD adx = DD.valueOf(a.x).selfSubtract(p.x);
    final DD ady = DD.valueOf(a.y).selfSubtract(p.y);
    final DD bdx = DD.valueOf(b.x).selfSubtract(p.x);
    final DD bdy = DD.valueOf(b.y).selfSubtract(p.y);
    final DD cdx = DD.valueOf(c.x).selfSubtract(p.x);
    final DD cdy = DD.valueOf(c.y).selfSubtract(p.y);

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
  public static boolean isInCircleDDSlow(final Coordinate a,
    final Coordinate b, final Coordinate c, final Coordinate p) {
    final DD px = DD.valueOf(p.x);
    final DD py = DD.valueOf(p.y);
    final DD ax = DD.valueOf(a.x);
    final DD ay = DD.valueOf(a.y);
    final DD bx = DD.valueOf(b.x);
    final DD by = DD.valueOf(b.y);
    final DD cx = DD.valueOf(c.x);
    final DD cy = DD.valueOf(c.y);

    final DD aTerm = (ax.multiply(ax).add(ay.multiply(ay))).multiply(triAreaDDSlow(
      bx, by, cx, cy, px, py));
    final DD bTerm = (bx.multiply(bx).add(by.multiply(by))).multiply(triAreaDDSlow(
      ax, ay, cx, cy, px, py));
    final DD cTerm = (cx.multiply(cx).add(cy.multiply(cy))).multiply(triAreaDDSlow(
      ax, ay, bx, by, px, py));
    final DD pTerm = (px.multiply(px).add(py.multiply(py))).multiply(triAreaDDSlow(
      ax, ay, bx, by, cx, cy));

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
  public static boolean isInCircleNonRobust(final Coordinate a,
    final Coordinate b, final Coordinate c, final Coordinate p) {
    final boolean isInCircle = (a.x * a.x + a.y * a.y) * triArea(b, c, p)
      - (b.x * b.x + b.y * b.y) * triArea(a, c, p) + (c.x * c.x + c.y * c.y)
      * triArea(a, b, p) - (p.x * p.x + p.y * p.y) * triArea(a, b, c) > 0;
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
  public static boolean isInCircleNormalized(final Coordinate a,
    final Coordinate b, final Coordinate c, final Coordinate p) {
    final double adx = a.x - p.x;
    final double ady = a.y - p.y;
    final double bdx = b.x - p.x;
    final double bdy = b.y - p.y;
    final double cdx = c.x - p.x;
    final double cdy = c.y - p.y;

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
  public static boolean isInCircleRobust(final Coordinate a,
    final Coordinate b, final Coordinate c, final Coordinate p) {
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
  private static double triArea(final Coordinate a, final Coordinate b,
    final Coordinate c) {
    return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
  }

  public static DD triAreaDDFast(final Coordinate a, final Coordinate b,
    final Coordinate c) {

    final DD t1 = DD.valueOf(b.x)
      .selfSubtract(a.x)
      .selfMultiply(DD.valueOf(c.y).selfSubtract(a.y));

    final DD t2 = DD.valueOf(b.y)
      .selfSubtract(a.y)
      .selfMultiply(DD.valueOf(c.x).selfSubtract(a.x));

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
  public static DD triAreaDDSlow(final DD ax, final DD ay, final DD bx,
    final DD by, final DD cx, final DD cy) {
    return (bx.subtract(ax).multiply(cy.subtract(ay)).subtract(by.subtract(ay)
      .multiply(cx.subtract(ax))));
  }

}
