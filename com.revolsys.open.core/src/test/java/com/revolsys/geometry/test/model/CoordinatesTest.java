package com.revolsys.geometry.test.model;

import java.util.Arrays;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.coordinates.CoordinatesUtil;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.model.impl.PointDoubleXYZ;
import com.revolsys.util.number.Doubles;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class CoordinatesTest extends TestCase {
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
    TestRunner.run(CoordinatesTest.class);
  }

  public CoordinatesTest(final String name) {
    super(name);
  }

  private void assertArcDistance(final double x1, final double y1, final double x2, final double y2,
    final int expectedCompare) {
    final int actualCompare = CoordinatesUtil.compareArcDistance(x1, y1, x2, y2);
    assertEquals("POINT(" + x1 + " " + y1 + "),POINT(" + x2 + " " + y2 + ")", expectedCompare,
      actualCompare);
  }

  public void testClone() {
    for (final Point point : Arrays.asList(//
      new PointDoubleXY(100.0, 200.0), //
      new PointDoubleXYZ(100.0, 200.0, 50.0), //
      new PointDouble(100.0, 200.0, 50.0, 4.0))) {
      final Point clone = point.newPoint();
      assertEquals3d(point, clone);
    }
  }

  public void testCompareArcDistance() {
    assertArcDistance(-1, -1, 0, 0, -1);
    assertArcDistance(0, 0, -1, -1, 1);
    assertArcDistance(0, 1, 1, 0, -1);
    assertArcDistance(1, 0, 0, 1, 1);
    assertArcDistance(0, 0, 0, 0, 0);
  }

  public void testCompareTo() {
    final Point lowest = new PointDoubleXYZ(10.0, 100.0, 50.0);
    final Point highest = new PointDoubleXYZ(20.0, 100.0, 50.0);
    final Point equalToHighest = new PointDoubleXYZ(20.0, 100.0, 50.0);
    final Point higherStill = new PointDoubleXYZ(20.0, 200.0, 50.0);

    assertEquals(-1, lowest.compareTo(highest));
    assertEquals(1, highest.compareTo(lowest));
    assertEquals(-1, highest.compareTo(higherStill));
    assertEquals(0, highest.compareTo(equalToHighest));
  }

  public void testConstructor2D() {
    final Point c = new PointDoubleXY(350.2, 4566.8);
    assertEquals(c, 350.2, 4566.8);
  }

  public void testConstructor3D() {
    final Point c = new PointDoubleXYZ(350.2, 4566.8, 5266.3);
    assertEquals(c, 350.2, 4566.8, 5266.3);
  }

  public void testDistance() {
    final Point coord1 = new PointDoubleXYZ(0.0, 0.0, 0.0);
    final Point coord2 = new PointDoubleXYZ(100.0, 200.0, 50.0);
    final double distance = coord1.distancePoint(coord2);
    assertEquals(distance, 223.60679774997897, 0.00001);
  }

  public void testDistance3D() {
    final Point coord1 = new PointDoubleXYZ(0.0, 0.0, 0.0);
    final Point coord2 = new PointDoubleXYZ(100.0, 200.0, 50.0);
    final double distance = coord1.distance3d(coord2);
    assertEquals(distance, 229.128784747792, 0.000001);
  }

  public void testEquals() {
    final Point c1 = new PointDoubleXYZ(1.0, 2, 3);
    final String s = "Not a coordinate";
    assertTrue(!c1.equals(s));

    final Point c2 = new PointDoubleXYZ(1.0, 2.0, 3.0);
    assertTrue(c1.equals(2, c2));

    final Point c3 = new PointDoubleXYZ(1.0, 22.0, 3.0);
    assertTrue(!c1.equals(2, c3));
  }

  public void testEquals2D() {
    final Point c1 = new PointDoubleXYZ(1.0, 2.0, 3.0);
    final Point c2 = new PointDoubleXYZ(1.0, 2.0, 3.0);
    assertTrue(c1.equals(2, c2));

    final Point c3 = new PointDoubleXYZ(1.0, 22.0, 3.0);
    assertTrue(!c1.equals(2, c3));
  }

  public void testEquals2DWithinTolerance() {
    final Point c = new PointDoubleXYZ(100.0, 200.0, 50.0);
    final Point aBitOff = new PointDoubleXYZ(100.1, 200.1, 50.0);
    assertTrue(c.equalsVertex2d(aBitOff, 0.2));
  }

  public void testEquals3D() {
    final Point c1 = new PointDoubleXYZ(1.0, 2.0, 3.0);
    final Point c2 = new PointDoubleXYZ(1.0, 2.0, 3.0);
    assertTrue(c1.equals(3, c2));

    final Point c3 = new PointDoubleXYZ(1.0, 22.0, 3.0);
    assertTrue(!c1.equals(3, c3));
  }

  public void testGetOrdinate() {
    final Point c = new PointDoubleXYZ(350.2, 4566.8, 5266.3);
    assertEquals(c.getCoordinate(Geometry.X), 350.2);
    assertEquals(c.getCoordinate(Geometry.Y), 4566.8);
    assertEquals(c.getCoordinate(Geometry.Z), 5266.3);
  }

  public void testToString() {
    final String expectedResult = "POINT Z(100 200 50)";
    final String actualResult = new PointDoubleXYZ(100.0, 200.0, 50.0).toString();
    assertEquals(expectedResult, actualResult);
  }
}
