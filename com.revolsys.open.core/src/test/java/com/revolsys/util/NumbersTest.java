package com.revolsys.util;

import org.junit.Assert;
import org.junit.Test;

public class NumbersTest {
  private void assertOverlaps(final int min1, final int max1, final int min2, final int max2,
    final boolean expected) {
    final boolean actual = Numbers.overlaps(min1, max1, min2, max2);
    final String message = min1 + "-" + max1 + " " + min2 + "-" + max2;
    Assert.assertEquals(message, expected, actual);
  }

  @Test
  public void testOverlaps() {
    // same range and min/max reversed
    assertOverlaps(0, 10, 0, 10, true);
    assertOverlaps(10, 0, 0, 10, true);
    assertOverlaps(0, 10, 10, 0, true);
    assertOverlaps(10, 0, 10, 0, true);

    // ends of range contained
    assertOverlaps(1, 1, 1, 1, true);
    assertOverlaps(1, 2, 1, 1, true);
    assertOverlaps(1, 2, 2, 2, true);
    assertOverlaps(1, 1, 1, 2, true);
    assertOverlaps(2, 2, 1, 2, true);

    // ends of range overlap
    assertOverlaps(0, 1, 1, 1, true);
    assertOverlaps(1, 1, 0, 1, true);
    assertOverlaps(1, 1, 1, 2, true);
    assertOverlaps(1, 2, 1, 1, true);

    // not overlap touching
    assertOverlaps(1, 1, 0, 0, false);
    assertOverlaps(1, 1, 2, 2, false);
    assertOverlaps(0, 0, 1, 1, false);
    assertOverlaps(2, 2, 1, 1, false);
    assertOverlaps(1, 2, 0, 0, false);
    assertOverlaps(1, 2, 3, 3, false);
    assertOverlaps(0, 0, 1, 2, false);
    assertOverlaps(3, 3, 1, 2, false);
  }
}
