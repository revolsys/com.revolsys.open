package com.revolsys.geometry.test.old.algorithm;

import com.revolsys.geometry.model.coordinates.LineSegmentUtil;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.impl.PointDoubleXY;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class CGAlgorithmsTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(CGAlgorithmsTest.class);
  }

  public CGAlgorithmsTest(final String name) {
    super(name);
  }

  public void testDistanceLineLineDisjointCollinear() {
    assertEquals(1.999699, LineSegmentUtil.distanceLineLine(new PointDoubleXY(0.0, 0),
      new PointDoubleXY(9.9, 1.4), new PointDouble(11.88, 1.68), new PointDouble(21.78, 3.08)),
      0.000001);
  }

  public void testDistancePointLine() {
    assertEquals(0.5, LineSegmentUtil.distanceLinePoint(new PointDoubleXY(0.0, 0),
      new PointDoubleXY(1.0, 0), new PointDouble(0.5, 0.5)), 0.000001);
    assertEquals(1.0, LineSegmentUtil.distanceLinePoint(new PointDoubleXY(0.0, 0),
      new PointDoubleXY(1.0, 0), new PointDouble(2.0, 0)), 0.000001);
  }

}
