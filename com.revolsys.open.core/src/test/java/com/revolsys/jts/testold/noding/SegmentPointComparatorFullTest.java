package com.revolsys.jts.testold.noding;

import junit.framework.TestCase;

import com.revolsys.gis.model.coordinates.AbstractCoordinates;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.PrecisionModel;
import com.revolsys.jts.noding.Octant;
import com.revolsys.jts.noding.SegmentPointComparator;

/**
 * Test IntersectionSegment#compareNodePosition using an exhaustive set
 * of test cases
 *
 * @version 1.7
 */
public class SegmentPointComparatorFullTest extends TestCase {

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(SegmentPointComparatorFullTest.class);
  }

  private final PrecisionModel pm = new PrecisionModel(1.0);

  public SegmentPointComparatorFullTest(final String name) {
    super(name);
  }

  private void checkNodePosition(final LineSegment seg, final Coordinates p0,
    final Coordinates p1, final int expectedPositionValue) {
    final int octant = Octant.octant(seg.getP0(), seg.getP1());
    final int posValue = SegmentPointComparator.compare(octant, p0, p1);
    System.out.println(octant + " " + p0 + " " + p1 + " " + posValue);
    assertTrue(posValue == expectedPositionValue);
  }

  private void checkPointsAtDistance(final LineSegment seg, final double dist0,
    final double dist1) {
    final Coordinates p0 = computePoint(seg, dist0);
    final Coordinates p1 = computePoint(seg, dist1);
    if (p0.equals(p1)) {
      checkNodePosition(seg, p0, p1, 0);
    } else {
      checkNodePosition(seg, p0, p1, -1);
      checkNodePosition(seg, p1, p0, 1);
    }
  }

  private void checkSegment(final double x, final double y) {
    final Coordinates seg0 = new Coordinate((double)0, 0, Coordinates.NULL_ORDINATE);
    final Coordinates seg1 = new Coordinate((double)x, y, Coordinates.NULL_ORDINATE);
    final LineSegment seg = new LineSegment(seg0, seg1);

    for (int i = 0; i < 4; i++) {
      final double dist = i;

      final double gridSize = 1 / this.pm.getScale();

      checkPointsAtDistance(seg, dist, dist + 1.0 * gridSize);
      checkPointsAtDistance(seg, dist, dist + 2.0 * gridSize);
      checkPointsAtDistance(seg, dist, dist + 3.0 * gridSize);
      checkPointsAtDistance(seg, dist, dist + 4.0 * gridSize);
    }
  }

  private Coordinates computePoint(final LineSegment seg, final double dist) {
    final double dx = seg.getP1().getX() - seg.getP0().getX();
    final double dy = seg.getP1().getY() - seg.getP0().getY();
    final double len = seg.getLength();
    final Coordinates pt = new Coordinate((double)dist * dx / len, dist * dy / len, Coordinates.NULL_ORDINATE);
    this.pm.makePrecise(pt);
    return pt;
  }

  public void testQuadrant0() {
    checkSegment(100, 0);
    checkSegment(100, 50);
    checkSegment(100, 100);
    checkSegment(100, 150);
    checkSegment(0, 100);
  }

  public void testQuadrant1() {
    checkSegment(-100, 0);
    checkSegment(-100, 50);
    checkSegment(-100, 100);
    checkSegment(-100, 150);
  }

  public void testQuadrant2() {
    checkSegment(-100, 0);
    checkSegment(-100, -50);
    checkSegment(-100, -100);
    checkSegment(-100, -150);
  }

  public void testQuadrant4() {
    checkSegment(100, -50);
    checkSegment(100, -100);
    checkSegment(100, -150);
    checkSegment(0, -100);
  }

}
