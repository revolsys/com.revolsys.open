package com.revolsys.jts.testold.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.CoordinateSequenceFactory;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.CoordinatesList;

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

  Coordinates[] createArray(final int size) {
    final Coordinates[] coords = new Coordinates[size];
    for (int i = 0; i < size; i++) {
      final double base = 2 * 1;
      coords[i] = new Coordinate((double)base, base + 1, base + 2);
    }
    return coords;
  }

  abstract CoordinateSequenceFactory getCSFactory();

  boolean isAllCoordsEqual(final CoordinatesList seq, final Coordinates coord) {
    for (int i = 0; i < seq.size(); i++) {
      if (!coord.equals(seq.getCoordinate(i))) {
        return false;
      }

      if (coord.getX() != seq.getValue(i, CoordinatesList.X)) {
        return false;
      }
      if (coord.getY() != seq.getValue(i, CoordinatesList.Y)) {
        return false;
      }
      if (coord.getZ() != seq.getValue(i, CoordinatesList.Z)) {
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
  boolean isEqual(final CoordinatesList seq, final Coordinates[] coords) {
    if (seq.size() != coords.length) {
      return false;
    }

    final Coordinates p = new Coordinate();

    for (int i = 0; i < seq.size(); i++) {
      if (!coords[i].equals(seq.getCoordinate(i))) {
        return false;
      }

      // Ordinate named getters
      if (coords[i].getX() != seq.getX(i)) {
        return false;
      }
      if (coords[i].getY() != seq.getY(i)) {
        return false;
      }

      // Ordinate indexed getters
      if (coords[i].getX() != seq.getValue(i, CoordinatesList.X)) {
        return false;
      }
      if (coords[i].getY() != seq.getValue(i, CoordinatesList.Y)) {
        return false;
      }
      if (coords[i].getZ() != seq.getValue(i, CoordinatesList.Z)) {
        return false;
      }

      // Coordinates getter
      seq.getCoordinate(i, p);
      if (coords[i].getX() != p.getX()) {
        return false;
      }
      if (coords[i].getY() != p.getY()) {
        return false;
      }
      if (coords[i].getZ() != p.getZ()) {
        return false;
      }

    }
    return true;
  }

  public void test2DZOrdinate() {
    final Coordinates[] coords = createArray(SIZE);

    final CoordinatesList seq = getCSFactory().create(SIZE, 2);
    for (int i = 0; i < seq.size(); i++) {
      seq.setValue(i, 0, coords[i].getX());
      seq.setValue(i, 1, coords[i].getY());
    }

    for (int i = 0; i < seq.size(); i++) {
      final Coordinates p = seq.getCoordinate(i);
      assertTrue(Double.isNaN(p.getZ()));
    }
  }

  public void testCreateByInit() {
    final Coordinates[] coords = createArray(SIZE);
    final CoordinatesList seq = getCSFactory().create(coords);
    assertTrue(isEqual(seq, coords));
  }

  public void testCreateByInitAndCopy() {
    final Coordinates[] coords = createArray(SIZE);
    CoordinateSequenceFactory factory = getCSFactory();
    final CoordinatesList seq = factory.create(coords);
    final CoordinatesList seq2 = factory.create(seq);
    assertTrue(isEqual(seq2, coords));
  }

  public void testCreateBySizeAndModify() {
    final Coordinates[] coords = createArray(SIZE);

    final CoordinatesList seq = getCSFactory().create(SIZE, 3);
    for (int i = 0; i < seq.size(); i++) {
      seq.setValue(i, 0, coords[i].getX());
      seq.setValue(i, 1, coords[i].getY());
      seq.setValue(i, 2, coords[i].getZ());
    }

    assertTrue(isEqual(seq, coords));
  }

  public void testZeroLength() {
    final CoordinatesList seq = getCSFactory().create(0, 3);
    assertTrue(seq.size() == 0);

    final CoordinatesList seq2 = getCSFactory().create((Coordinates[])null);
    assertTrue(seq2.size() == 0);
  }
}
