package com.revolsys.jts.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

public class CoordinateTest extends TestCase {
  public static void main(final String args[]) {
    TestRunner.run(CoordinateTest.class);
  }

  public CoordinateTest(final String name) {
    super(name);
  }

  public void testClone() {
    final Coordinate c = new Coordinate(100.0, 200.0, 50.0);
    final Coordinate clone = (Coordinate)c.clone();
    assertTrue(c.equals3D(clone));
  }

  public void testCompareTo() {
    final Coordinate lowest = new Coordinate(10.0, 100.0, 50.0);
    final Coordinate highest = new Coordinate(20.0, 100.0, 50.0);
    final Coordinate equalToHighest = new Coordinate(20.0, 100.0, 50.0);
    final Coordinate higherStill = new Coordinate(20.0, 200.0, 50.0);

    assertEquals(-1, lowest.compareTo(highest));
    assertEquals(1, highest.compareTo(lowest));
    assertEquals(-1, highest.compareTo(higherStill));
    assertEquals(0, highest.compareTo(equalToHighest));
  }

  public void testConstructor2D() {
    final Coordinate c = new Coordinate(350.2, 4566.8);
    assertEquals(c.x, 350.2);
    assertEquals(c.y, 4566.8);
    assertEquals(c.z, Coordinate.NULL_ORDINATE);
  }

  public void testConstructor3D() {
    final Coordinate c = new Coordinate(350.2, 4566.8, 5266.3);
    assertEquals(c.x, 350.2);
    assertEquals(c.y, 4566.8);
    assertEquals(c.z, 5266.3);
  }

  public void testCopyConstructor3D() {
    final Coordinate orig = new Coordinate(350.2, 4566.8, 5266.3);
    final Coordinate c = new Coordinate(orig);
    assertEquals(c.x, 350.2);
    assertEquals(c.y, 4566.8);
    assertEquals(c.z, 5266.3);
  }

  public void testDefaultConstructor() {
    final Coordinate c = new Coordinate();
    assertEquals(c.x, 0.0);
    assertEquals(c.y, 0.0);
    assertEquals(c.z, Coordinate.NULL_ORDINATE);
  }

  public void testDistance() {
    final Coordinate coord1 = new Coordinate(0.0, 0.0, 0.0);
    final Coordinate coord2 = new Coordinate(100.0, 200.0, 50.0);
    final double distance = coord1.distance(coord2);
    assertEquals(distance, 223.60679774997897, 0.00001);
  }

  public void testDistance3D() {
    final Coordinate coord1 = new Coordinate(0.0, 0.0, 0.0);
    final Coordinate coord2 = new Coordinate(100.0, 200.0, 50.0);
    final double distance = coord1.distance3D(coord2);
    assertEquals(distance, 229.128784747792, 0.000001);
  }

  public void testEquals() {
    final Coordinate c1 = new Coordinate(1, 2, 3);
    final String s = "Not a coordinate";
    assertTrue(!c1.equals(s));

    final Coordinate c2 = new Coordinate(1, 2, 3);
    assertTrue(c1.equals2D(c2));

    final Coordinate c3 = new Coordinate(1, 22, 3);
    assertTrue(!c1.equals2D(c3));
  }

  public void testEquals2D() {
    final Coordinate c1 = new Coordinate(1, 2, 3);
    final Coordinate c2 = new Coordinate(1, 2, 3);
    assertTrue(c1.equals2D(c2));

    final Coordinate c3 = new Coordinate(1, 22, 3);
    assertTrue(!c1.equals2D(c3));
  }

  public void testEquals2DWithinTolerance() {
    final Coordinate c = new Coordinate(100.0, 200.0, 50.0);
    final Coordinate aBitOff = new Coordinate(100.1, 200.1, 50.0);
    assertTrue(c.equals2D(aBitOff, 0.2));
  }

  public void testEquals3D() {
    final Coordinate c1 = new Coordinate(1, 2, 3);
    final Coordinate c2 = new Coordinate(1, 2, 3);
    assertTrue(c1.equals3D(c2));

    final Coordinate c3 = new Coordinate(1, 22, 3);
    assertTrue(!c1.equals3D(c3));
  }

  public void testEqualsInZ() {

    final Coordinate c = new Coordinate(100.0, 200.0, 50.0);
    final Coordinate withSameZ = new Coordinate(100.1, 200.1, 50.1);
    assertTrue(c.equalInZ(withSameZ, 0.2));
  }

  public void testGetOrdinate() {
    final Coordinate c = new Coordinate(350.2, 4566.8, 5266.3);
    assertEquals(c.getOrdinate(Coordinate.X), 350.2);
    assertEquals(c.getOrdinate(Coordinate.Y), 4566.8);
    assertEquals(c.getOrdinate(Coordinate.Z), 5266.3);
  }

  public void testSetCoordinate() {
    final Coordinate orig = new Coordinate(350.2, 4566.8, 5266.3);
    final Coordinate c = new Coordinate();
    c.setCoordinate(orig);
    assertEquals(c.x, 350.2);
    assertEquals(c.y, 4566.8);
    assertEquals(c.z, 5266.3);
  }

  public void testSetOrdinate() {
    final Coordinate c = new Coordinate();
    c.setOrdinate(Coordinate.X, 111);
    c.setOrdinate(Coordinate.Y, 222);
    c.setOrdinate(Coordinate.Z, 333);
    assertEquals(c.getOrdinate(Coordinate.X), 111.0);
    assertEquals(c.getOrdinate(Coordinate.Y), 222.0);
    assertEquals(c.getOrdinate(Coordinate.Z), 333.0);
  }

  public void testToString() {
    final String expectedResult = "(100.0, 200.0, 50.0)";
    final String actualResult = new Coordinate(100.0, 200.0, 50.0).toString();
    assertEquals(expectedResult, actualResult);
  }

}
