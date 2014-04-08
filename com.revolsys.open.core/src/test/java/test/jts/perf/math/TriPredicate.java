package test.jts.perf.math;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Triangle;
import com.revolsys.jts.math.DD;

/**
 * Algorithms for computing values and predicates
 * associated with triangles.
 * For some algorithms extended-precision
 * versions are provided, which are more robust
 * (i.e. they produce correct answers in more cases).
 * These are used in triangulation algorithms.
 * 
 * @author Martin Davis
 *
 */
public class TriPredicate {
  /**
   * Tests if a point is inside the circle defined by the points a, b, c. 
   * This test uses simple
   * double-precision arithmetic, and thus may not be robust.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircle(final Coordinate a, final Coordinate b,
    final Coordinate c, final Coordinate p) {
    final boolean isInCircle = (a.x * a.x + a.y * a.y) * triArea(b, c, p)
      - (b.x * b.x + b.y * b.y) * triArea(a, c, p) + (c.x * c.x + c.y * c.y)
      * triArea(a, b, p) - (p.x * p.x + p.y * p.y) * triArea(a, b, c) > 0;
    return isInCircle;
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

  /**
   * Tests if a point is inside the circle defined by the points a, b, c. 
   * The computation uses {@link DD} arithmetic for robustness.
   * 
   * @param a a vertex of the triangle
   * @param b a vertex of the triangle
   * @param c a vertex of the triangle
   * @param p the point to test
   * @return true if this point is inside the circle defined by the points a, b, c
   */
  public static boolean isInCircleDD(final Coordinate a, final Coordinate b,
    final Coordinate c, final Coordinate p) {
    final DD px = new DD(p.x);
    final DD py = new DD(p.y);
    final DD ax = new DD(a.x);
    final DD ay = new DD(a.y);
    final DD bx = new DD(b.x);
    final DD by = new DD(b.y);
    final DD cx = new DD(c.x);
    final DD cy = new DD(c.y);

    final DD aTerm = ax.multiply(ax)
      .add(ay.multiply(ay))
      .multiply(triAreaDD(bx, by, cx, cy, px, py));
    final DD bTerm = bx.multiply(bx)
      .add(by.multiply(by))
      .multiply(triAreaDD(ax, ay, cx, cy, px, py));
    final DD cTerm = cx.multiply(cx)
      .add(cy.multiply(cy))
      .multiply(triAreaDD(ax, ay, bx, by, px, py));
    final DD pTerm = px.multiply(px)
      .add(py.multiply(py))
      .multiply(triAreaDD(ax, ay, bx, by, cx, cy));

    final DD sum = aTerm.subtract(bTerm).add(cTerm).subtract(pTerm);
    final boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  public static boolean isInCircleDD2(final Coordinate a, final Coordinate b,
    final Coordinate c, final Coordinate p) {
    final DD aTerm = DD.sqr(a.x)
      .selfAdd(DD.sqr(a.y))
      .selfMultiply(triAreaDD2(b, c, p));
    final DD bTerm = DD.sqr(b.x)
      .selfAdd(DD.sqr(b.y))
      .selfMultiply(triAreaDD2(a, c, p));
    final DD cTerm = DD.sqr(c.x)
      .selfAdd(DD.sqr(c.y))
      .selfMultiply(triAreaDD2(a, b, p));
    final DD pTerm = DD.sqr(p.x)
      .selfAdd(DD.sqr(p.y))
      .selfMultiply(triAreaDD2(a, b, c));

    final DD sum = aTerm.selfSubtract(bTerm).selfAdd(cTerm).selfSubtract(pTerm);
    final boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  public static boolean isInCircleDD3(final Coordinate a, final Coordinate b,
    final Coordinate c, final Coordinate p) {
    final DD adx = DD.valueOf(a.x).selfSubtract(p.x);
    final DD ady = DD.valueOf(a.y).selfSubtract(p.y);
    final DD bdx = DD.valueOf(b.x).selfSubtract(p.x);
    final DD bdy = DD.valueOf(b.y).selfSubtract(p.y);
    final DD cdx = DD.valueOf(c.x).selfSubtract(p.x);
    final DD cdy = DD.valueOf(c.y).selfSubtract(p.y);

    final DD abdet = adx.multiply(bdy).selfSubtract(bdx.multiply(ady));
    final DD bcdet = bdx.multiply(cdy).selfSubtract(cdx.multiply(bdy));
    final DD cadet = cdx.multiply(ady).selfSubtract(adx.multiply(cdy));
    final DD alift = adx.multiply(adx).selfSubtract(ady.multiply(ady));
    final DD blift = bdx.multiply(bdx).selfSubtract(bdy.multiply(bdy));
    final DD clift = cdx.multiply(cdx).selfSubtract(cdy.multiply(cdy));

    final DD sum = alift.selfMultiply(bcdet)
      .selfAdd(blift.selfMultiply(cadet))
      .selfAdd(clift.selfMultiply(abdet));

    final boolean isInCircle = sum.doubleValue() > 0;

    return isInCircle;
  }

  /**
   * Tests if a point is inside the circle defined by the points a, b, c. 
   * This test uses robust computation.
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
    return isInCircleDD(a, b, c, p);
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
  public static DD triAreaDD(final DD ax, final DD ay, final DD bx,
    final DD by, final DD cx, final DD cy) {
    return bx.subtract(ax)
      .multiply(cy.subtract(ay))
      .subtract(by.subtract(ay).multiply(cx.subtract(ax)));
  }

  public static DD triAreaDD2(final Coordinate a, final Coordinate b,
    final Coordinate c) {

    final DD t1 = DD.valueOf(b.x)
      .selfSubtract(a.x)
      .selfMultiply(DD.valueOf(c.y).selfSubtract(a.y));

    final DD t2 = DD.valueOf(b.y)
      .selfSubtract(a.y)
      .selfMultiply(DD.valueOf(c.x).selfSubtract(a.x));

    return t1.selfSubtract(t2);
  }

}
