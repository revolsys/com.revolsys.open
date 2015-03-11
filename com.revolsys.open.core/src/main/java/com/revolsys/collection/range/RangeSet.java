package com.revolsys.collection.range;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.revolsys.collection.MultiIterator;
import com.revolsys.util.CollectionUtil;

public class RangeSet extends AbstractSet<Object> implements Iterable<Object> {

  private static void addPart(final RangeSet set, final List<AbstractRange<?>> crossProductRanges,
    final String fromValue, final String rangeSpec, final int partStart, final int partEnd) {
    final String toValue = rangeSpec.substring(partStart, partEnd);
    final AbstractRange<?> partRange = Ranges.create(fromValue, toValue);
    if (crossProductRanges == null) {
      set.addRange(partRange);
    } else {
      crossProductRanges.add(partRange);
    }
  }

  public static RangeSet create(final String rangeSpec) {
    final RangeSet set = new RangeSet();
    int partStart = 0;
    int partEnd = 0;
    boolean inRange = false;
    String rangeFirstPart = null;
    List<AbstractRange<?>> crossProductRanges = null;
    final int charCount = rangeSpec.length();
    for (int i = 0; i < charCount; i++) {
      final char character = rangeSpec.charAt(i);
      if (!Character.isWhitespace(character)) {
        switch (character) {
          case '+':
            if (crossProductRanges == null) {
              crossProductRanges = new ArrayList<>();
            }
            addPart(set, crossProductRanges, rangeFirstPart, rangeSpec, partStart, partEnd);
            partStart = i + 1;
            inRange = false;
          break;
          case '~':
            if (inRange) {
              throw new RangeInvalidException(
                "The ~ character cannot be used twice in a range, see *~* in "
                  + rangeSpec.substring(0, i) + "*~*" + rangeSpec.substring(i + 1));
            } else {
              rangeFirstPart = rangeSpec.substring(partStart, partEnd);
              partStart = i + 1;
              inRange = true;
            }
          break;
          case ',':
            addPart(set, crossProductRanges, rangeFirstPart, rangeSpec, partStart, partEnd);
            if (crossProductRanges != null) {
              set.add(new CrossProductRange(crossProductRanges));
            }
            partStart = i + 1;
            inRange = false;
            rangeFirstPart = null;
            crossProductRanges = null;
          break;
        }
      }
      partEnd = i + 1;
    }
    if (partStart < charCount) {
      addPart(set, crossProductRanges, rangeFirstPart, rangeSpec, partStart, partEnd);
    }
    if (crossProductRanges != null) {
      set.addRange(new CrossProductRange(crossProductRanges));
    }
    return set;
  }

  private int size;

  private final List<AbstractRange<?>> ranges = new LinkedList<>();

  public RangeSet() {
  }

  public RangeSet(final Iterable<Integer> values) {
    addAll(values);
  }

  @Override
  public boolean add(final Object value) {
    if (value == null) {
      return false;
    } else {
      for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator.hasNext();) {
        final AbstractRange<?> range = iterator.next();
        AbstractRange<?> newRange = range.expand(value);
        if (range == newRange) {
          return false;
        } else if (newRange != null) {
          this.size++;
          if (iterator.hasNext()) {
            final int nextIndex = iterator.nextIndex();
            final AbstractRange<?> nextRange = this.ranges.get(nextIndex);
            final AbstractRange<?> expandedRange = newRange.expand(nextRange);
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
          iterator.add(Ranges.create(value));
          return true;

        }
      }
      this.ranges.add(Ranges.create(value));
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

  public boolean addRange(final AbstractRange<?> addRange) {
    boolean added = false;
    if (addRange != null) {
      for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator.hasNext();) {
        final AbstractRange<?> range = iterator.next();
        AbstractRange<?> newRange = range.expand(addRange);
        if (range == newRange) {
          return false;
        } else if (newRange != null) {
          this.size -= range.size();
          this.size += newRange.size();
          if (iterator.hasNext()) {
            final int nextIndex = iterator.nextIndex();
            final AbstractRange<?> nextRange = this.ranges.get(nextIndex);
            final AbstractRange<?> expandedRange = newRange.expand(nextRange);
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

  public boolean addRange(final char from, final char to) {
    final AbstractRange<?> addRange = Ranges.create(from, to);
    return addRange(addRange);
  }

  public boolean addRange(final int from, final int to) {
    final LongRange addRange = new LongRange(from, to);
    return addRange(addRange);
  }

  public boolean addRange(final Object from, final Object to) {
    final AbstractRange<?> addRange = Ranges.create(from, to);
    return addRange(addRange);
  }

  public List<AbstractRange<?>> getRanges() {
    return new ArrayList<>(this.ranges);
  }

  @Override
  public boolean isEmpty() {
    return this.size == 0;
  }

  @Override
  public Iterator<Object> iterator() {
    return new MultiIterator<Object>(this.ranges);
  }

  @Override
  public boolean remove(final Object value) {
    if (value instanceof Integer) {
      final Integer intValue = (Integer)value;
      for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator.hasNext();) {
        final AbstractRange<?> range = iterator.next();
        if (range instanceof LongRange) {
          final LongRange longRange = (LongRange)range;

          if (longRange.contains(intValue)) {
            final long from = longRange.getFrom();
            final long to = longRange.getTo();
            if (from == intValue) {
              if (to != intValue) {
                final AbstractRange<?> newRange = new LongRange(intValue + 1, to);
                iterator.set(newRange);
              } else {
                iterator.remove();
              }
              this.size--;
              return true;
            } else if (to == intValue) {
              final LongRange newRange = new LongRange(from, intValue - 1);
              iterator.set(newRange);
              this.size--;
              return true;
            } else {
              final LongRange newRange1 = new LongRange(from, intValue - 1);
              iterator.set(newRange1);
              final LongRange newRange2 = new LongRange(intValue + 1, to);
              iterator.add(newRange2);
              this.size--;
              return true;
            }
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
    for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator.hasNext();) {
      final AbstractRange<?> range = iterator.next();
      if (range instanceof LongRange) {
        final LongRange longRange = (LongRange)range;

        final long rangeFrom = longRange.getFrom();
        final long rangeTo = longRange.getTo();
        if (from <= rangeFrom) {
          if (to < rangeFrom) {
            return removed;
          } else if (to >= rangeTo) {
            iterator.remove();
          } else {
            final AbstractRange<?> newRange = new LongRange(to + 1, rangeTo);
            this.size -= range.size();
            this.size += newRange.size();
            iterator.set(newRange);
          }
        } else if (from <= rangeTo) {
          if (to < rangeTo) {
            final AbstractRange<?> newRange1 = new LongRange(rangeFrom, from - 1);
            iterator.set(newRange1);
            final AbstractRange<?> newRange2 = new LongRange(to + 1, rangeTo);
            iterator.add(newRange2);
            this.size -= range.size();
            this.size += newRange1.size();
            this.size += newRange2.size();

          } else {
            final AbstractRange<?> newRange = new LongRange(rangeFrom, from - 1);
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
    }
    return removed;
  }

  @Override
  public int size() {
    return this.size;
  }

  public List<Object> toList() {
    return CollectionUtil.list(this);
  }

  @Override
  public String toString() {
    return CollectionUtil.toString(",", this.ranges);
  }
}
