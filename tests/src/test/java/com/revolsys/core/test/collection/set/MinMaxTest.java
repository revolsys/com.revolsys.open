package com.revolsys.core.test.collection.set;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.collection.range.IntMinMax;

public class MinMaxTest {

  private void assertMinMaxClip(final int min1, final int max1, final int min2, final int max2,
    final Integer expectedMin, final Integer expectedMax) {
    final IntMinMax minMax1 = new IntMinMax(min1, max1);
    final IntMinMax minMax2 = new IntMinMax(min2, max2);

    final IntMinMax expected;
    if (expectedMin == null) {
      expected = new IntMinMax();
    } else {
      expected = new IntMinMax(expectedMin, expectedMax);
    }

    final IntMinMax actual1 = minMax1.clip(minMax2);
    Assert.assertEquals(minMax1 + " clip" + minMax2, expected, actual1);

    final IntMinMax actual2 = minMax2.clip(minMax1);
    Assert.assertEquals(minMax2 + " clip" + minMax1, expected, actual2);
  }

  private void assertMinMaxContains(final int min1, final int max1, final int min2, final int max2,
    final boolean expected) {
    final IntMinMax minMax1 = new IntMinMax(min1, max1);
    final IntMinMax minMax2 = new IntMinMax(min2, max2);
    final boolean actual = minMax1.contains(minMax2);
    Assert.assertEquals(minMax1 + " contains" + minMax2, expected, actual);
  }

  private void assertMinMaxOverlaps(final int min1, final int max1, final int min2, final int max2,
    final boolean expected) {
    final IntMinMax minMax1 = new IntMinMax(min1, max1);
    final IntMinMax minMax2 = new IntMinMax(min2, max2);
    final boolean actual1 = minMax1.overlaps(minMax2);
    Assert.assertEquals(minMax1 + " overlaps" + minMax2, expected, actual1);
    final boolean actual2 = minMax2.overlaps(minMax1);
    Assert.assertEquals(minMax2 + " overlaps" + minMax1, expected, actual2);
  }

  @Test
  public void testMinMaxClip() {
    assertMinMaxClip(0, 0, 1, 1, null, null);
    assertMinMaxClip(0, 1, 2, 2, null, null);
    assertMinMaxClip(0, 1, 2, 3, null, null);

    assertMinMaxClip(0, 1, 1, 2, 1, 1);
    assertMinMaxClip(0, 1, 1, 1, 1, 1);
    assertMinMaxClip(1, 1, 1, 1, 1, 1);
    assertMinMaxClip(1, 2, 1, 1, 1, 1);
    assertMinMaxClip(1, 2, 1, 2, 1, 2);
    assertMinMaxClip(1, 4, 1, 4, 1, 4);
    assertMinMaxClip(1, 4, 1, 3, 1, 3);
    assertMinMaxClip(1, 4, 2, 3, 2, 3);
    assertMinMaxClip(1, 4, 2, 4, 2, 4);
  }

  @Test
  public void testMinMaxContains() {
    assertMinMaxContains(0, 0, 1, 1, false);
    assertMinMaxContains(0, 1, 1, 2, false);
    assertMinMaxContains(1, 1, 0, 1, false);
    assertMinMaxContains(0, 1, 2, 2, false);
    assertMinMaxContains(2, 2, 0, 1, false);
    assertMinMaxContains(0, 1, 2, 3, false);
    assertMinMaxContains(2, 3, 0, 1, false);

    assertMinMaxContains(0, 1, 1, 1, true);
    assertMinMaxContains(1, 1, 1, 1, true);
    assertMinMaxContains(1, 2, 1, 1, true);
    assertMinMaxContains(1, 2, 1, 2, true);
    assertMinMaxContains(1, 4, 1, 4, true);
    assertMinMaxContains(1, 4, 1, 3, true);
    assertMinMaxContains(1, 4, 2, 3, true);
    assertMinMaxContains(1, 4, 2, 4, true);
  }

  @Test
  public void testMinMaxOverlaps() {
    assertMinMaxOverlaps(0, 0, 1, 1, false);
    assertMinMaxOverlaps(0, 1, 2, 2, false);
    assertMinMaxOverlaps(0, 1, 2, 3, false);

    assertMinMaxOverlaps(0, 1, 1, 2, true);
    assertMinMaxOverlaps(0, 1, 1, 1, true);
    assertMinMaxOverlaps(1, 1, 1, 1, true);
    assertMinMaxOverlaps(1, 2, 1, 1, true);
    assertMinMaxOverlaps(1, 2, 1, 2, true);
    assertMinMaxOverlaps(1, 4, 1, 4, true);
    assertMinMaxOverlaps(1, 4, 1, 3, true);
    assertMinMaxOverlaps(1, 4, 2, 3, true);
    assertMinMaxOverlaps(1, 4, 2, 4, true);
  }

}
