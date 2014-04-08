package com.revolsys.jts.noding;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.LineSegment;
import com.revolsys.jts.geom.PrecisionModel;

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

  private void checkNodePosition(final LineSegment seg, final Coordinate p0,
    final Coordinate p1, final int expectedPositionValue) {
    final int octant = Octant.octant(seg.p0, seg.p1);
    final int posValue = SegmentPointComparator.compare(octant, p0, p1);
    System.out.println(octant + " " + p0 + " " + p1 + " " + posValue);
    assertTrue(posValue == expectedPositionValue);
  }

  private void checkPointsAtDistance(final LineSegment seg, final double dist0,
    final double dist1) {
    final Coordinate p0 = computePoint(seg, dist0);
    final Coordinate p1 = computePoint(seg, dist1);
    if (p0.equals(p1)) {
      checkNodePosition(seg, p0, p1, 0);
    } else {
      checkNodePosition(seg, p0, p1, -1);
      checkNodePosition(seg, p1, p0, 1);
    }
  }

  private void checkSegment(final double x, final double y) {
    final Coordinate seg0 = new Coordinate(0, 0);
    final Coordinate seg1 = new Coordinate(x, y);
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

  private Coordinate computePoint(final LineSegment seg, final double dist) {
    final double dx = seg.p1.x - seg.p0.x;
    final double dy = seg.p1.y - seg.p0.y;
    final double len = seg.getLength();
    final Coordinate pt = new Coordinate(dist * dx / len, dist * dy / len);
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
