package com.revolsys.jts.test.geometry;

import java.util.Arrays;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.gis.model.data.equals.NumberEquals;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;

public class CoordinateTest extends TestCase {
  public static void assertEquals(final Coordinates point,
    final double... coordinates) {
    for (int i = 0; i < coordinates.length; i++) {
      final double value = point.getValue(i);
      final double expected = coordinates[i];
      final boolean equal = NumberEquals.equal(value, expected);
      failNotEqual("Coordinate not equal " + i, equal, expected, value);

    }
  }

  public static void assertEquals3d(final Coordinates point1,
    final Coordinates point2) {
    final boolean equal = point1.equals3d(point2);
    failNotEqual("Not Equal 3D", equal, point1, point2);
    final boolean inverseEqual = point2.equals3d(point1);
    failNotEqual("Not Equal 3D Inverse", inverseEqual, point2, point1);
  }

  public static void failNotEqual(final String message, final boolean equal,
    final Object value1, final Object value2) {
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
    for (final Coordinates point : Arrays.asList(//
      new Coordinate(),//
      new Coordinate(100.0, 200.0), //
      new Coordinate(100.0, 200.0, 50.0), //
      new Coordinate(100.0, 200.0, 50.0, 4.0))) {
      final Coordinates clone = point.cloneCoordinates();
      assertEquals3d(point, clone);
    }
  }

  public void testCompareTo() {
    final Coordinates lowest = new Coordinate(10.0, 100.0, 50.0);
    final Coordinates highest = new Coordinate(20.0, 100.0, 50.0);
    final Coordinates equalToHighest = new Coordinate(20.0, 100.0, 50.0);
    final Coordinates higherStill = new Coordinate(20.0, 200.0, 50.0);

    assertEquals(-1, lowest.compareTo(highest));
    assertEquals(1, highest.compareTo(lowest));
    assertEquals(-1, highest.compareTo(higherStill));
    assertEquals(0, highest.compareTo(equalToHighest));
  }

  public void testConstructor2D() {
    final Coordinates c = new Coordinate(350.2, 4566.8,
      Coordinates.NULL_ORDINATE);
    assertEquals(c, 350.2, 4566.8, Coordinates.NULL_ORDINATE);
  }

  public void testConstructor3D() {
    final Coordinates c = new Coordinate(350.2, 4566.8, 5266.3);
    assertEquals(c, 350.2, 4566.8, 5266.3);
  }

  public void testCopyConstructor3D() {
    final Coordinates orig = new Coordinate(350.2, 4566.8, 5266.3);
    final Coordinates c = new Coordinate(orig);
    assertEquals(c, 350.2, 4566.8, 5266.3);
  }

  public void testDefaultConstructor() {
    final Coordinates c = new Coordinate();
    assertEquals(c, 0.0, 0.0, Coordinates.NULL_ORDINATE);
  }

  public void testDistance() {
    final Coordinates coord1 = new Coordinate(0.0, 0.0, 0.0);
    final Coordinates coord2 = new Coordinate(100.0, 200.0, 50.0);
    final double distance = coord1.distance(coord2);
    assertEquals(distance, 223.60679774997897, 0.00001);
  }

  public void testDistance3D() {
    final Coordinates coord1 = new Coordinate(0.0, 0.0, 0.0);
    final Coordinates coord2 = new Coordinate(100.0, 200.0, 50.0);
    final double distance = coord1.distance3d(coord2);
    assertEquals(distance, 229.128784747792, 0.000001);
  }

  public void testEquals() {
    final Coordinates c1 = new Coordinate(1.0, 2, 3);
    final String s = "Not a coordinate";
    assertTrue(!c1.equals(s));

    final Coordinates c2 = new Coordinate(1.0, 2.0, 3.0);
    assertTrue(c1.equals2d(c2));

    final Coordinates c3 = new Coordinate(1.0, 22.0, 3.0);
    assertTrue(!c1.equals2d(c3));
  }

  public void testEquals2D() {
    final Coordinates c1 = new Coordinate(1.0, 2.0, 3.0);
    final Coordinates c2 = new Coordinate(1.0, 2.0, 3.0);
    assertTrue(c1.equals2d(c2));

    final Coordinates c3 = new Coordinate(1.0, 22.0, 3.0);
    assertTrue(!c1.equals2d(c3));
  }

  public void testEquals2DWithinTolerance() {
    final Coordinates c = new Coordinate(100.0, 200.0, 50.0);
    final Coordinates aBitOff = new Coordinate(100.1, 200.1, 50.0);
    assertTrue(c.equals2d(aBitOff, 0.2));
  }

  public void testEquals3D() {
    final Coordinates c1 = new Coordinate(1.0, 2.0, 3.0);
    final Coordinates c2 = new Coordinate(1.0, 2.0, 3.0);
    assertTrue(c1.equals3d(c2));

    final Coordinates c3 = new Coordinate(1.0, 22.0, 3.0);
    assertTrue(!c1.equals3d(c3));
  }

  public void testGetOrdinate() {
    final Coordinates c = new Coordinate(350.2, 4566.8, 5266.3);
    assertEquals(c.getValue(Coordinates.X), 350.2);
    assertEquals(c.getValue(Coordinates.Y), 4566.8);
    assertEquals(c.getValue(Coordinates.Z), 5266.3);
  }

  public void testSetCoordinate() {
    final Coordinates orig = new Coordinate(350.2, 4566.8, 5266.3);
    final Coordinates c = new Coordinate(3);
    c.setCoordinate(orig);
    assertEquals(350.2, c.getX());
    assertEquals(4566.8, c.getY());
    assertEquals(5266.3, c.getZ());
  }

  public void testSetOrdinate() {
    final Coordinates c = new Coordinate(3);
    c.setValue(Coordinates.X, 111);
    c.setValue(Coordinates.Y, 222);
    c.setValue(Coordinates.Z, 333);
    assertEquals(c.getValue(Coordinates.X), 111.0);
    assertEquals(c.getValue(Coordinates.Y), 222.0);
    assertEquals(c.getValue(Coordinates.Z), 333.0);
  }

  public void testToString() {
    final String expectedResult = "POINT(100.0 200.0 50.0)";
    final String actualResult = new Coordinate(100.0, 200.0, 50.0).toString();
    assertEquals(expectedResult, actualResult);
  }

}
