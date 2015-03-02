package com.revolsys.collection.set;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.revolsys.collection.MultiIterator;
import com.revolsys.jts.util.NumberUtil;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class IntRangeSet extends AbstractSet<Integer> {

  public static IntRangeSet create(final String rangeSpec) {
    final IntRangeSet set = new IntRangeSet();
    if (Property.hasValue(rangeSpec)) {
      for (String part : rangeSpec.split(",+")) {
        part = part.trim();
        if (NumberUtil.isInteger(part)) {
          set.add(Integer.valueOf(part));
        } else if (part.matches("^\\d+-\\d+$")) {
          final int dashIndex = part.indexOf('-');
          final int from = Integer.valueOf(part.substring(0, dashIndex));
          final int to = Integer.valueOf(part.substring(dashIndex + 1));
          set.addRange(from, to);
        }

      }
    }
    return set;
  }

  private int size;

  private final List<IntRange> ranges = new LinkedList<>();

  public IntRangeSet() {
  }

  public IntRangeSet(final Iterable<Integer> values) {
    addAll(values);
  }

  @Override
  public boolean add(final Integer value) {
    if (value == null) {
      return false;
    } else {
      for (final ListIterator<IntRange> iterator = this.ranges.listIterator(); iterator.hasNext();) {
        final IntRange range = iterator.next();
        IntRange newRange = range.expand(value);
        if (range == newRange) {
          return false;
        } else if (newRange != null) {
          this.size++;
          if (iterator.hasNext()) {
            final int nextIndex = iterator.nextIndex();
            final IntRange nextRange = this.ranges.get(nextIndex);
            final IntRange expandedRange = newRange.expand(nextRange);
            if (expandedRange != null) {
              newRange = expandedRange;
              iterator.next();
              iterator.remove();
              iterator.previous();
            }
          }
          iterator.set(newRange);
          return true;
        } else if (range.compareFromValue(value) > 0) {
          iterator.previous();
          iterator.add(new IntRange(value));
          return true;

        }
      }
      this.ranges.add(new IntRange(value));
      this.size++;
      return true;
    }
  }

  public boolean addAll(final Iterable<Integer> values) {
    boolean added = false;
    for (final Integer value : values) {
      added |= add(value);
    }
    return added;
  }

  public boolean addRange(final int from, final int to) {
    final IntRange addRange = new IntRange(from, to);
    return addRange(addRange);
  }

  public boolean addRange(final IntRange addRange) {
    boolean added = false;
    if (addRange != null) {
      for (final ListIterator<IntRange> iterator = this.ranges.listIterator(); iterator.hasNext();) {
        final IntRange range = iterator.next();
        IntRange newRange = range.expand(addRange);
        if (range == newRange) {
          return false;
        } else if (newRange != null) {
          this.size -= range.size();
          this.size += newRange.size();
          if (iterator.hasNext()) {
            final int nextIndex = iterator.nextIndex();
            final IntRange nextRange = this.ranges.get(nextIndex);
            final IntRange expandedRange = newRange.expand(nextRange);
            if (expandedRange != null) {
              this.size -= newRange.size();
              this.size -= nextRange.size();

              newRange = expandedRange;
              iterator.next();
              iterator.remove();
              iterator.previous();
              this.size += newRange.size();
            }
          }
          iterator.set(newRange);
          added = true;
        } else if (!added && range.compareFromValue(addRange.getFrom()) > 0
          && (iterator.previousIndex() == 0 || range.compareFromValue(addRange.getTo()) > 0)) {
          this.ranges.add(iterator.previousIndex(), addRange);
          this.size += addRange.size();
          return true;
        }
      }
      if (!added) {
        this.ranges.add(addRange);
        this.size += addRange.size();
      }
    }
    return added;
  }

  public List<IntRange> getRanges() {
    return new ArrayList<>(this.ranges);
  }

  @Override
  public boolean isEmpty() {
    return this.size == 0;
  }

  @Override
  public Iterator<Integer> iterator() {
    return new MultiIterator<Integer>(this.ranges);
  }

  @Override
  public boolean remove(final Object value) {
    if (value instanceof Integer) {
      final Integer intValue = (Integer)value;
      for (final ListIterator<IntRange> iterator = this.ranges.listIterator(); iterator.hasNext();) {
        final IntRange range = iterator.next();
        if (range.contains(intValue)) {
          final int from = range.getFrom();
          final int to = range.getTo();
          if (from == intValue) {
            if (to != intValue) {
              final IntRange newRange = new IntRange(intValue + 1, to);
              iterator.set(newRange);
            } else {
              iterator.remove();
            }
            this.size--;
            return true;
          } else if (to == intValue) {
            final IntRange newRange = new IntRange(from, intValue - 1);
            iterator.set(newRange);
            this.size--;
            return true;
          } else {
            final IntRange newRange1 = new IntRange(from, intValue - 1);
            iterator.set(newRange1);
            final IntRange newRange2 = new IntRange(intValue + 1, to);
            iterator.add(newRange2);
            this.size--;
            return true;
          }
        }
      }
    }

    return false;
  }

  public boolean removeAll(final Iterable<Integer> values) {
    boolean removed = false;
    for (final Integer value : values) {
      removed |= remove(value);
    }
    return removed;
  }

  public boolean removeRange(final int from, final int to) {
    boolean removed = false;
    for (final ListIterator<IntRange> iterator = this.ranges.listIterator(); iterator.hasNext();) {
      final IntRange range = iterator.next();
      final int rangeFrom = range.getFrom();
      final int rangeTo = range.getTo();
      if (from <= rangeFrom) {
        if (to < rangeFrom) {
          return removed;
        } else if (to >= rangeTo) {
          iterator.remove();
        } else {
          final IntRange newRange = new IntRange(to + 1, rangeTo);
          this.size -= range.size();
          this.size += newRange.size();
          iterator.set(newRange);
        }
      } else if (from <= rangeTo) {
        if (to < rangeTo) {
          final IntRange newRange1 = new IntRange(rangeFrom, from - 1);
          iterator.set(newRange1);
          final IntRange newRange2 = new IntRange(to + 1, rangeTo);
          iterator.add(newRange2);
          this.size -= range.size();
          this.size += newRange1.size();
          this.size += newRange2.size();

        } else {
          final IntRange newRange = new IntRange(rangeFrom, from - 1);
          this.size -= range.size();
          this.size += newRange.size();
          iterator.set(newRange);
          if (to == rangeTo) {
            return true;
          }
          removed = true;
        }
      }

    }
    return removed;
  }

  @Override
  public int size() {
    return this.size;
  }

  public List<Integer> toList() {
    return CollectionUtil.list(this);
  }

  @Override
  public String toString() {
    return CollectionUtil.toString(",", this.ranges);
  }
}
