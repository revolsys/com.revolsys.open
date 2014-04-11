package com.revolsys.jts.testold.algorithm;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;

public class CGAlgorithmsTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(CGAlgorithmsTest.class);
  }

  public CGAlgorithmsTest(final String name) {
    super(name);
  }

  public void testDistanceLineLineDisjointCollinear() {
    assertEquals(1.999699, CGAlgorithms.distanceLineLine(new Coordinate(0.0, 0,
      Coordinates.NULL_ORDINATE), new Coordinate(9.9, 1.4,
      Coordinates.NULL_ORDINATE), new Coordinate(11.88, 1.68,
      Coordinates.NULL_ORDINATE), new Coordinate(21.78, 3.08,
      Coordinates.NULL_ORDINATE)), 0.000001);
  }

  public void testDistancePointLine() {
    assertEquals(0.5, CGAlgorithms.distancePointLine(new Coordinate(0.5, 0.5,
      Coordinates.NULL_ORDINATE), new Coordinate(0.0, 0,
      Coordinates.NULL_ORDINATE), new Coordinate(1.0, 0,
      Coordinates.NULL_ORDINATE)), 0.000001);
    assertEquals(1.0, CGAlgorithms.distancePointLine(new Coordinate(2.0, 0,
      Coordinates.NULL_ORDINATE), new Coordinate(0.0, 0,
      Coordinates.NULL_ORDINATE), new Coordinate(1.0, 0,
      Coordinates.NULL_ORDINATE)), 0.000001);
  }

  public void testDistancePointLinePerpendicular() {
    assertEquals(0.5, CGAlgorithms.distancePointLinePerpendicular(
      new Coordinate(0.5, 0.5, Coordinates.NULL_ORDINATE), new Coordinate(0.0,
        0, Coordinates.NULL_ORDINATE), new Coordinate(1.0, 0,
        Coordinates.NULL_ORDINATE)), 0.000001);
    assertEquals(0.5, CGAlgorithms.distancePointLinePerpendicular(
      new Coordinate(3.5, 0.5, Coordinates.NULL_ORDINATE), new Coordinate(0.0,
        0, Coordinates.NULL_ORDINATE), new Coordinate(1.0, 0,
        Coordinates.NULL_ORDINATE)), 0.000001);
    assertEquals(0.707106, CGAlgorithms.distancePointLinePerpendicular(
      new Coordinate(1.0, 0, Coordinates.NULL_ORDINATE), new Coordinate(0.0, 0,
        Coordinates.NULL_ORDINATE), new Coordinate(1.0, 1,
        Coordinates.NULL_ORDINATE)), 0.000001);
  }

}
