package com.revolsys.core.test.collection.set;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.revolsys.collection.range.AbstractRange;
import com.revolsys.collection.range.IntRange;
import com.revolsys.collection.range.RangeInvalidException;
import com.revolsys.collection.range.RangeSet;
import com.revolsys.collection.range.Ranges;

public class RangeTest {

  private static void assertCharRange(final char from, final char to, final char... characters) {
    final AbstractRange<?> range = Ranges.newRange(from, to);
    final List<Object> list = new ArrayList<>();
    for (final char character : characters) {
      list.add(character);
    }
    Assert.assertEquals(list.size(), range.size());
    if (from == to) {
      Assert.assertEquals(Character.toString(from), range.toString());
    } else {
      if (from <= to) {
        Assert.assertEquals(from + "~" + to, range.toString());
      } else {
        Assert.assertEquals(to + "~" + from, range.toString());
      }
    }
    Assert.assertEquals(list, range.toList());
    int i = 0;
    for (final Object character : range) {
      final Object expectedValue = list.get(i);
      Assert.assertEquals("element " + i, character, expectedValue);
      i++;
    }

  }

  private static void assertRange(final int from, final int to, final int... numbers) {
    final IntRange range = new IntRange(from, to);
    final List<Integer> list = new ArrayList<>();
    for (final int i : numbers) {
      list.add(i);
    }
    Assert.assertEquals(list.size(), range.size());
    if (from == to) {
      Assert.assertEquals(Integer.toString(from), range.toString());
    } else {
      if (from <= to) {
        Assert.assertEquals(from + "~" + to, range.toString());
      } else {
        Assert.assertEquals(to + "~" + from, range.toString());
      }
    }
    Assert.assertEquals(list, range.toList());
    int i = 0;
    for (final int value : range) {
      final int expectedValue = list.get(i);
      if (value != expectedValue) {
        Assert.fail("range(" + i + ") " + value + "!=" + expectedValue);
      }
      i++;
    }

  }

  private static void assertRangeSetAdd(final RangeSet set, final Object value,
    final String expected) {
    set.add(value);
    Assert.assertEquals(expected, set.toString());
  }

  private static void assertRangeSetAddRange(final RangeSet set, final int from, final int to,
    final String expected) {
    set.addRange(from, to);
    Assert.assertEquals(expected, set.toString());
  }

  private static void assertRangeSetCreate(final String range, final String expected) {
    final RangeSet set = RangeSet.newRangeSet(range);
    Assert.assertEquals(expected, set.toString());

    final RangeSet expectedSet = RangeSet.newRangeSet(expected);
    Assert.assertEquals(expectedSet, set);

    final int size = set.size();
    final int expectedSize = expectedSet.size();
    Assert.assertEquals(expectedSize, size);

    final List<Object> list = set.toList();
    final List<Object> expectedList = expectedSet.toList();
    Assert.assertEquals(expectedList, list);
  }

  private static void assertRangeSetException(final String range) {
    try {
      final RangeSet set = RangeSet.newRangeSet(range);
      Assert.fail("Range is not supposed to be valid " + range + ", created " + set);
    } catch (final RangeInvalidException e) {
    }
  }

  private static void assertRangeSetRemove(final RangeSet set, final int value,
    final String expected) {
    set.remove(value);
    Assert.assertEquals(expected, set.toString());
  }

  private static void assertRangeSetRemoveRange(final RangeSet set, final int from, final int to,
    final String expected) {
    set.removeRange(from, to);
    Assert.assertEquals(expected, set.toString());
  }

  @Test
  public void testCharRange() {
    Assert.assertEquals("A", Ranges.newRange('A').toString());
    Assert.assertEquals("A~E", Ranges.newRange('A', 'E').toString());
    Assert.assertEquals("A~E", Ranges.newRange('E', 'A').toString());
    Assert.assertEquals("0~9", Ranges.newRange('0', '9').toString());
    Assert.assertEquals("0~9", Ranges.newRange('9', '0').toString());

    assertCharRange('A', 'A', 'A');
    assertCharRange('Z', 'Z', 'Z');
    assertCharRange('A', 'C', 'A', 'B', 'C');
    assertCharRange('C', 'A', 'A', 'B', 'C');
    assertCharRange('Z', 'X', 'X', 'Y', 'Z');

    Assert.assertEquals("a", Ranges.newRange('a').toString());
    Assert.assertEquals("a~e", Ranges.newRange('a', 'e').toString());

    assertCharRange('a', 'a', 'a');
    assertCharRange('z', 'z', 'z');
    assertCharRange('a', 'c', 'a', 'b', 'c');
    assertCharRange('c', 'a', 'a', 'b', 'c');
    assertCharRange('a', 'c', 'a', 'b', 'c');
    assertCharRange('z', 'x', 'x', 'y', 'z');

    assertRangeSetException("a~Z");
    assertRangeSetException("Z~a");
  }

  @Test
  public void testCompare() {
    Assert.assertEquals(0, AbstractRange.compare((byte)0, 0));
    Assert.assertEquals(-1, AbstractRange.compare((byte)0, 1));
    Assert.assertEquals(1, AbstractRange.compare((byte)1, 0));

    Assert.assertEquals(0, AbstractRange.compare(0, (byte)0));
    Assert.assertEquals(-1, AbstractRange.compare(0, (byte)1));
    Assert.assertEquals(1, AbstractRange.compare(1, (byte)0));

    Assert.assertEquals(0, AbstractRange.compare((short)0, 0));
    Assert.assertEquals(-1, AbstractRange.compare((short)0, 1));
    Assert.assertEquals(1, AbstractRange.compare((short)1, 0));

    Assert.assertEquals(0, AbstractRange.compare(0, (short)0));
    Assert.assertEquals(-1, AbstractRange.compare(0, (short)1));
    Assert.assertEquals(1, AbstractRange.compare(1, (short)0));

    // int
    Assert.assertEquals(0, AbstractRange.compare(0, 0));
    Assert.assertEquals(-1, AbstractRange.compare(0, 1));
    Assert.assertEquals(1, AbstractRange.compare(1, 0));

    Assert.assertEquals(0, AbstractRange.compare((long)0, 0));
    Assert.assertEquals(-1, AbstractRange.compare((long)0, 1));
    Assert.assertEquals(1, AbstractRange.compare((long)1, 0));

    Assert.assertEquals(0, AbstractRange.compare(0, (long)0));
    Assert.assertEquals(-1, AbstractRange.compare(0, (long)1));
    Assert.assertEquals(1, AbstractRange.compare(1, (long)0));

    Assert.assertEquals(0, AbstractRange.compare((float)0, 0));
    Assert.assertEquals(-1, AbstractRange.compare((float)0, 1));
    Assert.assertEquals(1, AbstractRange.compare((float)1, 0));

    Assert.assertEquals(0, AbstractRange.compare(0, (float)0));
    Assert.assertEquals(-1, AbstractRange.compare(0, (float)1));
    Assert.assertEquals(1, AbstractRange.compare(1, (float)0));

    Assert.assertEquals(0, AbstractRange.compare((double)0, 0));
    Assert.assertEquals(-1, AbstractRange.compare((double)0, 1));
    Assert.assertEquals(1, AbstractRange.compare((double)1, 0));

    Assert.assertEquals(0, AbstractRange.compare(0, (double)0));
    Assert.assertEquals(-1, AbstractRange.compare(0, (double)1));
    Assert.assertEquals(1, AbstractRange.compare(1, (double)0));

    // Assert.assertEquals(0, AbstractRange.compare(0, "0"));
    // Assert.assertEquals(-1, AbstractRange.compare(0, "1"));
    // Assert.assertEquals(1, AbstractRange.compare(1, "0"));
    //
    // Assert.assertEquals(0, AbstractRange.compare("0", 0));
    // Assert.assertEquals(-1, AbstractRange.compare("0", 1));
    // Assert.assertEquals(1, AbstractRange.compare("1", 0));
    //
    // Assert.assertEquals(0, AbstractRange.compare(0, '0'));
    // Assert.assertEquals(-1, AbstractRange.compare(0, '1'));
    // Assert.assertEquals(1, AbstractRange.compare(1, '0'));
    //
    // Assert.assertEquals(0, AbstractRange.compare('0', 0));
    // Assert.assertEquals(-1, AbstractRange.compare('0', 1));
    // Assert.assertEquals(1, AbstractRange.compare('1', 0));

  }

  @Test
  public void testExpand() {
    final IntRange range1 = new IntRange(100, 110);
    Assert.assertEquals("Same", range1, range1.expand(range1));

    final IntRange range2 = new IntRange(100, 105);
    final IntRange range3 = new IntRange(100, 115);
    Assert.assertEquals("From 2nd Subset", range1, range1.expand(range2));
    Assert.assertEquals("From 2nd Subset Switched", range1, range2.expand(range1));
    Assert.assertEquals("From 2nd Superset", range3, range1.expand(range3));
    Assert.assertEquals("From 2nd Superset Switched", range3, range3.expand(range1));

    final IntRange range4 = new IntRange(105, 110);
    final IntRange range5 = new IntRange(-5, 110);
    Assert.assertEquals("To 2nd Subset", range1, range1.expand(range4));
    Assert.assertEquals("To 2nd Subset Switched", range1, range4.expand(range1));
    Assert.assertEquals("To 2nd Superset", range5, range1.expand(range5));
    Assert.assertEquals("To 2nd Superset Switched", range5, range5.expand(range1));

    final IntRange range6 = new IntRange(101, 109);
    Assert.assertEquals("Subset Middle", range1, range1.expand(range6));
    Assert.assertEquals("Subset Middle Switched", range1, range6.expand(range1));

    final IntRange range7 = new IntRange(98, 101);
    Assert.assertEquals("Overlap From", new IntRange(98, 110), range1.expand(range7));
    Assert.assertEquals("Overlap To", new IntRange(98, 110), range7.expand(range1));

    final IntRange range8 = new IntRange(98, 99);
    Assert.assertEquals("Touching From", new IntRange(98, 110), range1.expand(range8));
    Assert.assertEquals("Touching To", new IntRange(98, 110), range8.expand(range1));

    final IntRange range9 = new IntRange(0, 98);
    Assert.assertNull("Disjoint Before ", range1.expand(range9));
    Assert.assertNull("Disjoint After", range9.expand(range1));
  }

  @Test
  public void testIntRange() {
    Assert.assertEquals("1", new IntRange(1).toString());
    Assert.assertEquals("1~10", new IntRange(1, 10).toString());
    Assert.assertEquals("-10~-1", new IntRange(-10, -1).toString());

    assertRange(0, 0, 0);
    assertRange(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
    assertRange(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    assertRange(0, 3, 0, 1, 2, 3);
    assertRange(3, 0, 0, 1, 2, 3);
    assertRange(-1, 1, -1, 0, 1);
    assertRange(1, -1, -1, 0, 1);
  }

  @Test
  public void testRangeSet() {
    final RangeSet rangeSet = new RangeSet();
    assertRangeSetAdd(rangeSet, 1, "1");
    assertRangeSetAdd(rangeSet, 1, "1");
    assertRangeSetAdd(rangeSet, 2, "1~2");
    assertRangeSetAdd(rangeSet, 3, "1~3");
    assertRangeSetAdd(rangeSet, 0, "0~3");
    assertRangeSetAdd(rangeSet, 9, "0~3,9");
    assertRangeSetAdd(rangeSet, 8, "0~3,8~9");
    assertRangeSetAdd(rangeSet, 5, "0~3,5,8~9");
    assertRangeSetAdd(rangeSet, 6, "0~3,5~6,8~9");
    assertRangeSetAdd(rangeSet, 3, "0~3,5~6,8~9");
    assertRangeSetAdd(rangeSet, 4, "0~6,8~9");
    assertRangeSetAdd(rangeSet, 7, "0~9");

    assertRangeSetRemove(rangeSet, 0, "1~9");
    assertRangeSetRemove(rangeSet, 1, "2~9");
    assertRangeSetRemove(rangeSet, 0, "2~9");
    assertRangeSetRemove(rangeSet, 1, "2~9");
    assertRangeSetRemove(rangeSet, 9, "2~8");
    assertRangeSetRemove(rangeSet, 9, "2~8");
    assertRangeSetRemove(rangeSet, 5, "2~4,6~8");

    assertRangeSetAddRange(rangeSet, 1, 2, "1~4,6~8");
    assertRangeSetAddRange(rangeSet, 4, 5, "1~8");
    assertRangeSetAddRange(rangeSet, 0, 9, "0~9");
    assertRangeSetAddRange(rangeSet, -10, -2, "-10~-2,0~9");
    assertRangeSetAddRange(rangeSet, 11, 20, "-10~-2,0~9,11~20");

    // From <
    assertRangeSetRemoveRange(rangeSet, -12, -11, "-10~-2,0~9,11~20");
    assertRangeSetRemoveRange(rangeSet, -12, -10, "-9~-2,0~9,11~20");
    assertRangeSetRemoveRange(rangeSet, -12, -8, "-7~-2,0~9,11~20");
    assertRangeSetRemoveRange(rangeSet, -12, -2, "0~9,11~20");
    assertRangeSetRemoveRange(rangeSet, -1, 11, "12~20");

    // Reset Range
    assertRangeSetAddRange(rangeSet, -10, -2, "-10~-2,12~20");
    assertRangeSetAddRange(rangeSet, 0, 9, "-10~-2,0~9,12~20");
    assertRangeSetAddRange(rangeSet, 11, 20, "-10~-2,0~9,11~20");

    // From =
    assertRangeSetRemoveRange(rangeSet, -10, -10, "-9~-2,0~9,11~20");
    assertRangeSetRemoveRange(rangeSet, -9, -7, "-6~-2,0~9,11~20");
    assertRangeSetRemoveRange(rangeSet, -6, -2, "0~9,11~20");
    assertRangeSetRemoveRange(rangeSet, 0, 11, "12~20");

    // Reset Range
    assertRangeSetAddRange(rangeSet, -10, -2, "-10~-2,12~20");
    assertRangeSetAddRange(rangeSet, 0, 9, "-10~-2,0~9,12~20");
    assertRangeSetAddRange(rangeSet, 11, 20, "-10~-2,0~9,11~20");

    // From >
    assertRangeSetRemoveRange(rangeSet, -9, -9, "-10,-8~-2,0~9,11~20");
    assertRangeSetRemoveRange(rangeSet, -4, -2, "-10,-8~-5,0~9,11~20");
    assertRangeSetRemoveRange(rangeSet, -7, 0, "-10,-8,1~9,11~20");
    assertRangeSetRemoveRange(rangeSet, 9, 11, "-10,-8,1~8,12~20");
    assertRangeSetRemoveRange(rangeSet, 9, 12, "-10,-8,1~8,13~20");
    assertRangeSetRemoveRange(rangeSet, -10, 14, "15~20");

    assertRangeSetCreate("1", "1");
    assertRangeSetCreate("1~10", "1~10");
    assertRangeSetCreate("1,2~11", "1~11");
    assertRangeSetCreate("1~2,2~11", "1~11");
    assertRangeSetCreate("1~2,3~5,6~7", "1~7");
    assertRangeSetCreate("1~2,6~7,3~5,0~9", "0~9");

    final RangeSet rangeSet2 = new RangeSet();
    assertRangeSetAdd(rangeSet2, 1, "1");
    assertRangeSetAdd(rangeSet2, "1", "1");
    assertRangeSetAdd(rangeSet2, "a", "1,a");
    assertRangeSetAdd(rangeSet2, "a", "1,a");
    assertRangeSetAdd(rangeSet2, "e", "1,a,e");
    assertRangeSetAdd(rangeSet2, "b", "1,a~b,e");
    assertRangeSetAdd(rangeSet2, "A", "1,A,a~b,e");
    assertRangeSetAdd(rangeSet2, "C", "1,A,C,a~b,e");
    assertRangeSetAdd(rangeSet2, "B", "1,A~C,a~b,e");

    assertRangeSetCreate("A,B", "A~B");
    assertRangeSetCreate("1,B", "1,B");
    assertRangeSetCreate("01,B", "1,B");
  }

}
