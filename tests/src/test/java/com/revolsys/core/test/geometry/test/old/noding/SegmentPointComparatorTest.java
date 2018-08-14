package com.revolsys.core.test.geometry.test.old.noding;

import com.revolsys.geometry.model.impl.PointDoubleXY;
import com.revolsys.geometry.noding.SegmentPointComparator;

import junit.framework.TestCase;

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

  private void checkNodePosition(final int octant, final double x0, final double y0,
    final double x1, final double y1, final int expectedPositionValue) {
    final int posValue = SegmentPointComparator.compare(octant, new PointDoubleXY(x0, y0),
      new PointDoubleXY(x1, y1));
    assertTrue(posValue == expectedPositionValue);
  }

  public void testOctant0() {
    checkNodePosition(0, 1, 1, 2, 2, -1);
    checkNodePosition(0, 1, 0, 1, 1, -1);
  }
}
