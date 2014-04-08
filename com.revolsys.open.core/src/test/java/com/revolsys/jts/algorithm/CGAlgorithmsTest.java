package com.revolsys.jts.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Coordinate;

public class CGAlgorithmsTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(CGAlgorithmsTest.class);
  }

  public CGAlgorithmsTest(final String name) {
    super(name);
  }

  public void testDistanceLineLineDisjointCollinear() {
    assertEquals(1.999699, CGAlgorithms.distanceLineLine(new Coordinate(0, 0),
      new Coordinate(9.9, 1.4), new Coordinate(11.88, 1.68), new Coordinate(
        21.78, 3.08)), 0.000001);
  }

  public void testDistancePointLine() {
    assertEquals(0.5, CGAlgorithms.distancePointLine(new Coordinate(0.5, 0.5),
      new Coordinate(0, 0), new Coordinate(1, 0)), 0.000001);
    assertEquals(1.0, CGAlgorithms.distancePointLine(new Coordinate(2, 0),
      new Coordinate(0, 0), new Coordinate(1, 0)), 0.000001);
  }

  public void testDistancePointLinePerpendicular() {
    assertEquals(0.5, CGAlgorithms.distancePointLinePerpendicular(
      new Coordinate(0.5, 0.5), new Coordinate(0, 0), new Coordinate(1, 0)),
      0.000001);
    assertEquals(0.5, CGAlgorithms.distancePointLinePerpendicular(
      new Coordinate(3.5, 0.5), new Coordinate(0, 0), new Coordinate(1, 0)),
      0.000001);
    assertEquals(0.707106, CGAlgorithms.distancePointLinePerpendicular(
      new Coordinate(1, 0), new Coordinate(0, 0), new Coordinate(1, 1)),
      0.000001);
  }

}
