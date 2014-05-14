package com.revolsys.jts.testold.geom;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import com.revolsys.gis.model.coordinates.list.CoordinatesListUtil;
import com.revolsys.gis.model.coordinates.list.DoubleCoordinatesList;
import com.revolsys.jts.geom.PointList;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

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

  Point[] createArray(final int size) {
    final Point[] coords = new Point[size];
    for (int i = 0; i < size; i++) {
      final double base = 2 * 1;
      coords[i] = new PointDouble(base, base + 1, base + 2);
    }
    return coords;
  }

  boolean isAllCoordsEqual(final PointList seq, final Point coord) {
    for (int i = 0; i < seq.size(); i++) {
      if (!coord.equals(seq.getCoordinate(i))) {
        return false;
      }

      if (coord.getX() != seq.getValue(i, PointList.X)) {
        return false;
      }
      if (coord.getY() != seq.getValue(i, PointList.Y)) {
        return false;
      }
      if (coord.getZ() != seq.getValue(i, PointList.Z)) {
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
  boolean isEqual(final PointList seq, final Point[] coords) {
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
      if (coords[i].getX() != seq.getValue(i, PointList.X)) {
        return false;
      }
      if (coords[i].getY() != seq.getValue(i, PointList.Y)) {
        return false;
      }
      if (coords[i].getZ() != seq.getValue(i, PointList.Z)) {
        return false;
      }

      // Point getter
      final Point p = seq.getCoordinate(i);
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
    final Point[] coords = createArray(SIZE);

    final double[] coordinates = new double[SIZE * 2];
    for (int i = 0; i < SIZE; i++) {
      final Point point = coords[i];
      CoordinatesListUtil.setCoordinates(coordinates, 2, i, point);
    }
    final PointList seq = new DoubleCoordinatesList(2, coordinates);

    for (int i = 0; i < seq.size(); i++) {
      final Point p = seq.getCoordinate(i);
      assertTrue(Double.isNaN(p.getZ()));
    }
  }

  public void testCreateByInit() {
    final Point[] coords = createArray(SIZE);
    final PointList seq = new DoubleCoordinatesList(coords);
    assertTrue(isEqual(seq, coords));
  }

  public void testCreateBySizeAndModify() {
    final Point[] coords = createArray(SIZE);

    final double[] coordinates = new double[SIZE * 3];
    for (int i = 0; i < SIZE; i++) {
      final Point point = coords[i];
      CoordinatesListUtil.setCoordinates(coordinates, 3, i, point);
    }
    final PointList seq = new DoubleCoordinatesList(3, coordinates);

    assertTrue(isEqual(seq, coords));
  }

  public void testZeroLength() {
    final PointList seq = new DoubleCoordinatesList(0, 3);
    assertTrue(seq.size() == 0);

    final PointList seq2 = new DoubleCoordinatesList(0);
    assertTrue(seq2.size() == 0);
  }
}
