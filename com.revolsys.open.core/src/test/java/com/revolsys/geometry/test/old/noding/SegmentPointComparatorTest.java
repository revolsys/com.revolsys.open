package com.revolsys.geometry.test.old.noding;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.impl.PointDouble;
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
    final int posValue = SegmentPointComparator.compare(octant,
      new PointDouble(x0, y0, Geometry.NULL_ORDINATE),
      new PointDouble(x1, y1, Geometry.NULL_ORDINATE));
    assertTrue(posValue == expectedPositionValue);
  }

  public void testOctant0() {
    checkNodePosition(0, 1, 1, 2, 2, -1);
    checkNodePosition(0, 1, 0, 1, 1, -1);
  }
}
