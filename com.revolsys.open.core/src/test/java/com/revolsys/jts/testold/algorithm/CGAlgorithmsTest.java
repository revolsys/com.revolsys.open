package com.revolsys.jts.testold.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.gis.model.coordinates.LineSegmentUtil;
import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

public class CGAlgorithmsTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(CGAlgorithmsTest.class);
  }

  public CGAlgorithmsTest(final String name) {
    super(name);
  }

  public void testDistanceLineLineDisjointCollinear() {
    assertEquals(1.999699, LineSegmentUtil.distanceLineLine(new PointDouble(0.0, 0),
      new PointDouble(9.9, 1.4), new PointDouble(11.88, 1.68), new PointDouble(21.78, 3.08)),
      0.000001);
  }

  public void testDistancePointLine() {
    assertEquals(0.5, LineSegmentUtil.distanceLinePoint(new PointDouble(0.0, 0), new PointDouble(
      1.0, 0), new PointDouble(0.5, 0.5)), 0.000001);
    assertEquals(1.0, LineSegmentUtil.distanceLinePoint(new PointDouble(0.0, 0), new PointDouble(
      1.0, 0), new PointDouble(2.0, 0)), 0.000001);
  }

  public void testDistancePointLinePerpendicular() {
    assertEquals(0.5, CGAlgorithms.distancePointLinePerpendicular(new PointDouble(0.5, 0.5),
      new PointDouble(0.0, 0), new PointDouble(1.0, 0, Point.NULL_ORDINATE)), 0.000001);
    assertEquals(0.5, CGAlgorithms.distancePointLinePerpendicular(new PointDouble(3.5, 0.5),
      new PointDouble(0.0, 0), new PointDouble(1.0, 0, Point.NULL_ORDINATE)), 0.000001);
    assertEquals(0.707106, CGAlgorithms.distancePointLinePerpendicular(new PointDouble(1.0, 0),
      new PointDouble(0.0, 0, Point.NULL_ORDINATE), new PointDouble(1.0, 1, Point.NULL_ORDINATE)),
      0.000001);
  }

}
