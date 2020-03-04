package com.revolsys.core.test.geometry.test.model;

import java.util.Arrays;

import org.jeometry.common.number.Doubles;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class CoordinateTest extends TestCase {
  public static void assertEquals(final Point point, final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double value = point.getCoordinate(i);
      final double expected = coordinates[i];
      final boolean equal = Doubles.equal(value, expected);
      failNotEqual("Coordinate not equal " + i, equal, expected, value);

    }
  }

  public static void assertEquals3d(final Point point1, final Point point2) {
    final boolean equal = point1.equals(3, point2);
    failNotEqual("Not Equal 3D", equal, point1, point2);
    final boolean inverseEqual = point2.equals(3, point1);
    failNotEqual("Not Equal 3D Inverse", inverseEqual, point2, point1);
  }

  public static void failNotEqual(final String message, final boolean equal, final Object value1,
    final Object value2) {
    if (!equal) {
      failNotEquals(message, value1, value2);
    }
  }

  public static void main(final String args[]) {
    TestRunner.run(CoordinateTest.class);
  }

  public CoordinateTest(final String name) {
    super(name);
  }

  public void testClone() {
    for (final Point point : Arrays.asList(//
      new PointDouble(), //
      new PointDouble(100.0, 200.0), //
      new PointDouble(100.0, 200.0, 50.0), //
      new PointDouble(100.0, 200.0, 50.0, 4.0))) {
      final Point clone = point.newPoint2D();
      assertEquals3d(point, clone);
    }
  }

  public void testCompareTo() {
    final Point lowest = new PointDouble(10.0, 100.0, 50.0);
    final Point highest = new PointDouble(20.0, 100.0, 50.0);
    final Point equalToHighest = new PointDouble(20.0, 100.0, 50.0);
    final Point higherStill = new PointDouble(20.0, 200.0, 50.0);

    assertEquals(-1, lowest.compareTo(highest));
    assertEquals(1, highest.compareTo(lowest));
    assertEquals(-1, highest.compareTo(higherStill));
    assertEquals(0, highest.compareTo(equalToHighest));
  }

  public void testConstructor2D() {
    final Point c = new PointDouble(350.2, 4566.8);
    assertEquals(c, 350.2, 4566.8);
  }

  public void testConstructor3D() {
    final Point c = new PointDouble(350.2, 4566.8, 5266.3);
    assertEquals(c, 350.2, 4566.8, 5266.3);
  }

  public void testCopyConstructor3D() {
    final Point orig = new PointDouble(350.2, 4566.8, 5266.3);
    final Point c = orig;
    assertEquals(c, 350.2, 4566.8, 5266.3);
  }

  public void testDistance() {
    final Point coord1 = new PointDouble(0.0, 0.0, 0.0);
    final Point coord2 = new PointDouble(100.0, 200.0, 50.0);
    final double distance = coord1.distanceGeometry(coord2);
    assertEquals(distance, 223.60679774997897, 0.00001);
  }

  public void testDistance3D() {
    final Point coord1 = new PointDouble(0.0, 0.0, 0.0);
    final Point coord2 = new PointDouble(100.0, 200.0, 50.0);
    final double distance = coord1.distance3d(coord2);
    assertEquals(distance, 229.128784747792, 0.000001);
  }

  public void testEquals() {
    final Point c1 = new PointDouble(1.0, 2, 3);
    final String s = "Not a coordinate";
    assertTrue(!c1.equals(s));

    final Point c2 = new PointDouble(1.0, 2.0, 3.0);
    assertTrue(c1.equals(2, c2));

    final Point c3 = new PointDouble(1.0, 22.0, 3.0);
    assertTrue(!c1.equals(2, c3));
  }

  public void testEquals2D() {
    final Point c1 = new PointDouble(1.0, 2.0, 3.0);
    final Point c2 = new PointDouble(1.0, 2.0, 3.0);
    assertTrue(c1.equals(2, c2));

    final Point c3 = new PointDouble(1.0, 22.0, 3.0);
    assertTrue(!c1.equals(2, c3));
  }

  public void testEquals2DWithinTolerance() {
    final Point c = new PointDouble(100.0, 200.0, 50.0);
    final Point aBitOff = new PointDouble(100.1, 200.1, 50.0);
    assertTrue(c.equalsVertex2d(aBitOff, 0.2));
  }

  public void testEquals3D() {
    final Point c1 = new PointDouble(1.0, 2.0, 3.0);
    final Point c2 = new PointDouble(1.0, 2.0, 3.0);
    assertTrue(c1.equals(3, c2));

    final Point c3 = new PointDouble(1.0, 22.0, 3.0);
    assertTrue(!c1.equals(3, c3));
  }

  public void testGetOrdinate() {
    final Point c = new PointDouble(350.2, 4566.8, 5266.3);
    assertEquals(c.getCoordinate(Geometry.X), 350.2);
    assertEquals(c.getCoordinate(Geometry.Y), 4566.8);
    assertEquals(c.getCoordinate(Geometry.Z), 5266.3);
  }

  public void testToString() {
    final String expectedResult = "POINT Z(100 200 50)";
    final String actualResult = new PointDouble(100.0, 200.0, 50.0).toString();
    assertEquals(expectedResult, actualResult);
  }

}
