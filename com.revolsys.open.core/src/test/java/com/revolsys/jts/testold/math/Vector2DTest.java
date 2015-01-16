package com.revolsys.jts.testold.math;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.math.Vector2D;

public class Vector2DTest extends TestCase {

  public static void main(final String args[]) {
    TestRunner.run(Vector2DTest.class);
  }

  private static final double TOLERANCE = 1E-5;

  public Vector2DTest(final String name) {
    super(name);
  }

  void assertEquals(final Vector2D v1, final Vector2D v2) {
    assertTrue(v1.equals(v2));
  }

  void assertEquals(final Vector2D v1, final Vector2D v2, final double tolerance) {
    assertEquals(v1.getX(), v2.getX(), tolerance);
    assertEquals(v1.getY(), v2.getY(), tolerance);
  }

  public void testIsParallel() throws Exception {
    assertTrue(Vector2D.create(0, 1).isParallel(Vector2D.create(0, 2)));
    assertTrue(Vector2D.create(1, 1).isParallel(Vector2D.create(2, 2)));
    assertTrue(Vector2D.create(-1, -1).isParallel(Vector2D.create(2, 2)));

    assertTrue(!Vector2D.create(1, -1).isParallel(Vector2D.create(2, 2)));
  }

  public void testLength() {
    assertEquals(Vector2D.create(0, 1).length(), 1.0, TOLERANCE);
    assertEquals(Vector2D.create(0, -1).length(), 1.0, TOLERANCE);
    assertEquals(Vector2D.create(1, 1).length(), Math.sqrt(2.0), TOLERANCE);
    assertEquals(Vector2D.create(3, 4).length(), 5, TOLERANCE);
  }

  public void testToCoordinate() {
    assertEquals(Vector2D.create(Vector2D.create(1, 2).toCoordinate()),
      Vector2D.create(1, 2), TOLERANCE);
  }
}
