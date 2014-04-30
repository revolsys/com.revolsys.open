package com.revolsys.jts.testold.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.Coordinate;
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
      coords[i] = new Coordinate(base, base + 1, base + 2);
    }
    return coords;
  }

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
      final Coordinates p = seq.getCoordinate(i);
      final double pX = p.getX();
      if (coords[i].getX() != pX) {
        return false;
      }
      final double pY = p.getY();
      if (coords[i].getY() != pY) {
        return false;
      }
      final double pZ = p.getZ();
      if (coords[i].getZ() != pZ) {
        return false;
      }

    }
    return true;
  }

  public void test2DZOrdinate() {
    final Coordinates[] coords = createArray(SIZE);

    final CoordinatesList seq = new DoubleCoordinatesList(SIZE, 2);
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
    final CoordinatesList seq = new DoubleCoordinatesList(coords);
    assertTrue(isEqual(seq, coords));
  }

  public void testCreateBySizeAndModify() {
    final Coordinates[] coords = createArray(SIZE);

    final CoordinatesList seq = new DoubleCoordinatesList(SIZE, 3);
    for (int i = 0; i < seq.size(); i++) {
      seq.setValue(i, 0, coords[i].getX());
      seq.setValue(i, 1, coords[i].getY());
      seq.setValue(i, 2, coords[i].getZ());
    }

    assertTrue(isEqual(seq, coords));
  }

  public void testZeroLength() {
    final CoordinatesList seq = new DoubleCoordinatesList(0, 3);
    assertTrue(seq.size() == 0);

    final CoordinatesList seq2 = new DoubleCoordinatesList(0);
    assertTrue(seq2.size() == 0);
  }
}
