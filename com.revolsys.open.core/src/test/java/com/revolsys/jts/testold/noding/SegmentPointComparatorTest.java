package com.revolsys.jts.testold.noding;

import junit.framework.TestCase;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.noding.SegmentPointComparator;

/**
 * Test IntersectionSegment#compareNodePosition
 *
 * @version 1.7
 */
public class SegmentPointComparatorTest extends TestCase {

  public static void main(final String[] args) {
    junit.textui.TestRunner.run(SegmentPointComparatorTest.class);
  }

  public SegmentPointComparatorTest(final String name) {
    super(name);
  }

  private void checkNodePosition(final int octant, final double x0,
    final double y0, final double x1, final double y1,
    final int expectedPositionValue) {
    final int posValue = SegmentPointComparator.compare(octant, new Coordinate((double)
      x0, y0, Coordinates.NULL_ORDINATE), new Coordinate((double)x1, y1, Coordinates.NULL_ORDINATE));
    assertTrue(posValue == expectedPositionValue);
  }

  public void testOctant0() {
    checkNodePosition(0, 1, 1, 2, 2, -1);
    checkNodePosition(0, 1, 0, 1, 1, -1);
  }
}