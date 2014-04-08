package com.revolsys.jts.geom.impl;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateSequence;
import com.revolsys.jts.geom.CoordinateSequenceFactory;

/**
 * General test cases for CoordinateSequences.
 * Subclasses can set the factory to test different kinds of CoordinateSequences.
 *
 * @version 1.7
 */
public abstract class CoordinateSequenceTestBase extends TestCase {
  public static final int SIZE = 100;

  public static void main(final String args[]) {
    TestRunner.run(CoordinateSequenceTestBase.class);
  }

  public CoordinateSequenceTestBase(final String name) {
    super(name);
  }

  Coordinate[] createArray(final int size) {
    final Coordinate[] coords = new Coordinate[size];
    for (int i = 0; i < size; i++) {
      final double base = 2 * 1;
      coords[i] = new Coordinate(base, base + 1, base + 2);
    }
    return coords;
  }

  abstract CoordinateSequenceFactory getCSFactory();

  boolean isAllCoordsEqual(final CoordinateSequence seq, final Coordinate coord) {
    for (int i = 0; i < seq.size(); i++) {
      if (!coord.equals(seq.getCoordinate(i))) {
        return false;
      }

      if (coord.x != seq.getOrdinate(i, CoordinateSequence.X)) {
        return false;
      }
      if (coord.y != seq.getOrdinate(i, CoordinateSequence.Y)) {
        return false;
      }
      if (coord.z != seq.getOrdinate(i, CoordinateSequence.Z)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests for equality using all supported accessors,
   * to provides test coverage for them.
   * 
   * @param seq
   * @param coords
   * @return
   */
  boolean isEqual(final CoordinateSequence seq, final Coordinate[] coords) {
    if (seq.size() != coords.length) {
      return false;
    }

    final Coordinate p = new Coordinate();

    for (int i = 0; i < seq.size(); i++) {
      if (!coords[i].equals(seq.getCoordinate(i))) {
        return false;
      }

      // Ordinate named getters
      if (coords[i].x != seq.getX(i)) {
        return false;
      }
      if (coords[i].y != seq.getY(i)) {
        return false;
      }

      // Ordinate indexed getters
      if (coords[i].x != seq.getOrdinate(i, CoordinateSequence.X)) {
        return false;
      }
      if (coords[i].y != seq.getOrdinate(i, CoordinateSequence.Y)) {
        return false;
      }
      if (coords[i].z != seq.getOrdinate(i, CoordinateSequence.Z)) {
        return false;
      }

      // Coordinate getter
      seq.getCoordinate(i, p);
      if (coords[i].x != p.x) {
        return false;
      }
      if (coords[i].y != p.y) {
        return false;
      }
      if (coords[i].z != p.z) {
        return false;
      }

    }
    return true;
  }

  public void test2DZOrdinate() {
    final Coordinate[] coords = createArray(SIZE);

    final CoordinateSequence seq = getCSFactory().create(SIZE, 2);
    for (int i = 0; i < seq.size(); i++) {
      seq.setOrdinate(i, 0, coords[i].x);
      seq.setOrdinate(i, 1, coords[i].y);
    }

    for (int i = 0; i < seq.size(); i++) {
      final Coordinate p = seq.getCoordinate(i);
      assertTrue(Double.isNaN(p.z));
    }
  }

  public void testCreateByInit() {
    final Coordinate[] coords = createArray(SIZE);
    final CoordinateSequence seq = getCSFactory().create(coords);
    assertTrue(isEqual(seq, coords));
  }

  public void testCreateByInitAndCopy() {
    final Coordinate[] coords = createArray(SIZE);
    final CoordinateSequence seq = getCSFactory().create(coords);
    final CoordinateSequence seq2 = getCSFactory().create(seq);
    assertTrue(isEqual(seq2, coords));
  }

  public void testCreateBySizeAndModify() {
    final Coordinate[] coords = createArray(SIZE);

    final CoordinateSequence seq = getCSFactory().create(SIZE, 3);
    for (int i = 0; i < seq.size(); i++) {
      seq.setOrdinate(i, 0, coords[i].x);
      seq.setOrdinate(i, 1, coords[i].y);
      seq.setOrdinate(i, 2, coords[i].z);
    }

    assertTrue(isEqual(seq, coords));
  }

  public void testZeroLength() {
    final CoordinateSequence seq = getCSFactory().create(0, 3);
    assertTrue(seq.size() == 0);

    final CoordinateSequence seq2 = getCSFactory().create((Coordinate[])null);
    assertTrue(seq2.size() == 0);
  }
}
