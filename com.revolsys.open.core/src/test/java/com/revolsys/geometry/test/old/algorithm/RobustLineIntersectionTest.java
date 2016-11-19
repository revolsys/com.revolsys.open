package com.revolsys.geometry.test.old.algorithm;

import com.revolsys.geometry.algorithm.LineIntersector;
import com.revolsys.geometry.algorithm.RobustLineIntersector;
import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.wkb.ParseException;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests robustness and correctness of RobustLineIntersector
 * in some tricky cases.
 * Failure modes can include exceptions thrown, or incorrect
 * results returned.
 *
 * @author Owner
 *
 */
public class RobustLineIntersectionTest extends TestCase {
  public static boolean equals(final Point p0, final Point p1, final double distanceTolerance) {
    return p0.distancePoint(p1) <= distanceTolerance;
  }

  public static void main(final String args[]) {
    TestRunner.run(RobustLineIntersectionTest.class);
  }

  private final GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private final GeometryFactory geometryFactory2d = GeometryFactory.DEFAULT_2D;

  public RobustLineIntersectionTest(final String name) {
    super(name);
  }

  /**
   * Check that intersection of segment defined by points in pt array
   * is equal to the expectedIntPt value (up to the given distanceTolerance).
   *
   * @param pt
   * @param expectedIntersectionNum
   * @param distanceTolerance tolerance to use for equality test
   * @param expectedIntPt the expected intersection points (maybe null if not tested)
   */
  void checkIntersection(final Point[] pt, final int expectedIntersectionNum,
    final double distanceTolerance, final Point... expectedIntPt) {
    final LineIntersector li = new RobustLineIntersector();
    li.computeIntersectionPoints(pt[0], pt[1], pt[2], pt[3]);

    final int intNum = li.getIntersectionCount();
    assertEquals("Number of intersections not as expected", expectedIntersectionNum, intNum);

    if (expectedIntPt != null) {
      assertEquals("Wrong number of expected int pts provided", intNum, expectedIntPt.length);
      // test that both points are represented here
      final boolean isIntPointsCorrect = true;
      if (intNum == 1) {
        checkIntPoints(expectedIntPt[0], li.getIntersection(0), distanceTolerance);
      } else if (intNum == 2) {
        checkIntPoints(expectedIntPt[1], li.getIntersection(0), distanceTolerance);
        checkIntPoints(expectedIntPt[1], li.getIntersection(0), distanceTolerance);

        if (!(equals(expectedIntPt[0], li.getIntersection(0), distanceTolerance)
          || equals(expectedIntPt[0], li.getIntersection(1), distanceTolerance))) {
          checkIntPoints(expectedIntPt[0], li.getIntersection(0), distanceTolerance);
          checkIntPoints(expectedIntPt[0], li.getIntersection(1), distanceTolerance);
        } else if (!(equals(expectedIntPt[1], li.getIntersection(0), distanceTolerance)
          || equals(expectedIntPt[1], li.getIntersection(1), distanceTolerance))) {
          checkIntPoints(expectedIntPt[1], li.getIntersection(0), distanceTolerance);
          checkIntPoints(expectedIntPt[1], li.getIntersection(1), distanceTolerance);
        }
      }
    }
  }

  void checkIntersection(final String wkt1, final String wkt2, final int expectedIntersectionNum,
    final double distanceTolerance, final Point... intPt) throws ParseException {
    final LineString l1 = (LineString)this.geometryFactory.geometry(wkt1);
    final LineString l2 = (LineString)this.geometryFactory.geometry(wkt2);
    final Point[] pt = new Point[] {
      l1.getPoint(0), l1.getPoint(1), l2.getPoint(0), l2.getPoint(1)
    };
    checkIntersection(pt, expectedIntersectionNum, distanceTolerance, intPt);
  }

  void checkIntersection(final String wkt1, final String wkt2, final int expectedIntersectionNum,
    final String expectedWKT, final double distanceTolerance) throws ParseException {
    final LineString l1 = (LineString)this.geometryFactory.geometry(wkt1);
    final LineString l2 = (LineString)this.geometryFactory.geometry(wkt2);
    final Point[] pt = new Point[] {
      l1.getPoint(0), l1.getPoint(1), l2.getPoint(0), l2.getPoint(1)
    };
    final Geometry g = this.geometryFactory2d.geometry(expectedWKT);
    final Point[] intPt = CoordinatesListUtil.getPointArray(g);
    checkIntersection(pt, expectedIntersectionNum, distanceTolerance, intPt);
  }

  void checkIntersectionNone(final String wkt1, final String wkt2) throws ParseException {
    final LineString l1 = (LineString)this.geometryFactory.geometry(wkt1);
    final LineString l2 = (LineString)this.geometryFactory.geometry(wkt2);
    final Point[] pt = new Point[] {
      l1.getPoint(0), l1.getPoint(1), l2.getPoint(0), l2.getPoint(1)
    };
    checkIntersection(pt, 0, 0, null);
  }

  void checkIntPoints(final Point expectedPt, final Point actualPt,
    final double distanceTolerance) {
    final boolean isEqual = equals(expectedPt, actualPt, distanceTolerance);
    assertTrue("Int Pts not equal - " + "expected " + expectedPt + " VS " + "actual " + actualPt,
      isEqual);
  }

  public void test01TouchFromFrom() throws ParseException {
    checkIntersection("LINESTRING (2 2,0 0)", "LINESTRING (2 2,4 4)", 1, 0,
      new PointDoubleXY(2, 2));
  }

  public void test02TouchFromTo() throws ParseException {
    checkIntersection("LINESTRING (2 2,0 0)", "LINESTRING (4 4,2 2)", 1, 0,
      new PointDoubleXY(2, 2));
  }

  public void test03TouchToFrom() throws ParseException {
    checkIntersection("LINESTRING (0 0,2 2)", "LINESTRING (2 2,4 4)", 1, 0,
      new PointDoubleXY(2, 2));
  }

  public void test04TouchToTo() throws ParseException {
    checkIntersection("LINESTRING (0 0,2 2)", "LINESTRING (4 4,2 2)", 1, 0,
      new PointDoubleXY(2, 2));
  }

  public void test10Crossing() throws ParseException {
    checkIntersection("LINESTRING (0 0,2 2)", "LINESTRING (0 2,2 0)", 1, 0,
      new PointDoubleXY(1, 1));
  }

  /**
   * Following cases were failures when using the CentralEndpointIntersector heuristic.
   * This is because one segment lies at a significant angle to the other,
   * with only one endpoint is close to the other segment.
   * The CE heuristic chose the wrong endpoint to return.
   * The fix is to use a new heuristic which out of the 4 endpoints
   * chooses the one which is closest to the other segment.
   * This works in all known failure cases.
   *
   * @throws ParseException
   */
  public void testCentralEndpointHeuristicFailure() throws ParseException {
    checkIntersection("LINESTRING(163.81867067 -211.31840378,165.9174252 -214.1665075)",
      "LINESTRING(2.84139601 -57.95412726,469.59990601 -502.63851732)", 1,
      "POINT(163.81867067 -211.31840378)", 0);
  }

  public void testCentralEndpointHeuristicFailure2() throws ParseException {
    checkIntersection(
      "LINESTRING(-58.00593335955 -1.43739086465,-513.86101637525 -457.29247388035)",
      "LINESTRING(-215.22279674875 -158.65425425385,-218.1208801283 -160.68343590235)", 1,
      "POINT(-215.22279674875 -158.65425425385)", 0);
  }

  /**
   * Result of this test should be the same as the WKT one!
   * @throws ParseException
   */
  public void testCmp5CaseRaw() throws ParseException {
    checkIntersection(new Point[] {
      new PointDoubleXY(4348433.262114629, 5552595.478385733),
      new PointDoubleXY(4348440.849387404, 5552599.272022122),

      new PointDoubleXY(4348433.26211463, 5552595.47838573),
      new PointDoubleXY(4348440.8493874, 5552599.27202212)
    }, 1, 0, new Point[] {
      new PointDoubleXY(4348440.8493874, 5552599.27202212),
    });
  }

  /**
   * Outside envelope using HCoordinate method.
   *
   * @throws ParseException
   */
  public void testCmp5CaseWKT() throws ParseException {
    checkIntersection(
      "LINESTRING(4348433.262114629 5552595.478385733,4348440.849387404 5552599.272022122)",
      "LINESTRING(4348433.26211463 5552595.47838573,4348440.8493874 5552599.27202212)", 1, 0,
      new Point[] {
        new PointDoubleXY(4348440.8493874, 5552599.27202212),
      });
  }

  /**
   * Test from strk which is bad in GEOS (2009-04-14).
   *
   * @throws ParseException
   */
  public void testGEOS_1() throws ParseException {
    checkIntersection(
      "LINESTRING (588750.7429703881 4518950.493668233, 588748.2060409798 4518933.9452804085)",
      "LINESTRING (588745.824857241 4518940.742239175, 588748.2060437313 4518933.9452791475)", 1,
      "POINT (588748.2060416829 4518933.945284994)", 0);
  }

  /**
   * Test from strk which is bad in GEOS (2009-04-14).
   *
   * @throws ParseException
   */
  public void testGEOS_2() throws ParseException {
    checkIntersection(
      "LINESTRING (588743.626135934 4518924.610969561, 588732.2822865889 4518925.4314047815)",
      "LINESTRING (588739.1191384895 4518927.235700594, 588731.7854614238 4518924.578370095)", 1,
      "POINT (588733.8306132929 4518925.319423238)", 0);
  }

  /**
   * Test involving two non-almost-parallel lines.
   * Does not seem to cause problems with basic line intersection algorithm.
   *
   * @throws ParseException
   */
  public void testLeduc_1() throws ParseException {
    checkIntersection(
      "LINESTRING(305690.0434123494 254176.46578338774,305601.9999843455 254243.19999846347)",
      "LINESTRING(305689.6153764265 254177.33102743194,305692.4999844298 254171.4999983967)", 1,
      "POINT(305690.04341234936 254176.4657833878)", 0);
  }

  /**
   * Test from Tomas Fa - JTS list 6/13/2012
   *
   * Fails using original JTS DeVillers determine orientation test.
   * Succeeds using DD and Shewchuk orientation
   *
   * @throws ParseException
   */
  public void testTomasFa_1() throws ParseException {
    checkIntersectionNone("LINESTRING (-42.0 163.2, 21.2 265.2)",
      "LINESTRING (-26.2 188.7, 37.0 290.7)");
  }

  /**
   * Test from Tomas Fa - JTS list 6/13/2012
   *
   * Fails using original JTS DeVillers determine orientation test.
   * Succeeds using DD and Shewchuk orientation
   *
   * @throws ParseException
   */
  public void testTomasFa_2() throws ParseException {
    checkIntersectionNone("LINESTRING (-5.9 163.1, 76.1 250.7)",
      "LINESTRING (14.6 185.0, 96.6 272.6)");
  }
}
