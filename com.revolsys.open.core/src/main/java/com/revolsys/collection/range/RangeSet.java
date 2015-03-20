package com.revolsys.collection.range;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.revolsys.collection.MultiIterator;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public class RangeSet extends AbstractSet<Object> implements Iterable<Object>, Cloneable {

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

  public static RangeSet create(final int from, final int to) {
    final RangeSet range = new RangeSet();
    range.addRange(from, to);
    return range;
  }

  public static RangeSet create(final String rangeSpec) {
    final RangeSet set = new RangeSet();
    if (Property.hasValue(rangeSpec)) {
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
    }
    return set;
  }

  private int size;

  private List<AbstractRange<?>> ranges = new LinkedList<>();

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

  @Override
  public RangeSet clone() {
    try {
      final RangeSet clone = (RangeSet)super.clone();
      clone.ranges = new ArrayList<>(this.ranges);
      return clone;
    } catch (final CloneNotSupportedException e) {
      return ExceptionUtil.throwUncheckedException(e);
    }
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
    for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator.hasNext();) {
      final AbstractRange<?> range = iterator.next();
      if (range.contains(value)) {
        final Object from = range.getFrom();
        final Object to = range.getTo();
        if (EqualsRegistry.equal(from, value)) {
          if (EqualsRegistry.equal(to, value)) {
            iterator.remove();
          } else {
            final Object next = range.next(value);
            final AbstractRange<?> newRange = range.createNew(next, to);
            iterator.set(newRange);
          }
          this.size--;
          return true;
        } else if (EqualsRegistry.equal(to, value)) {
          final Object previous = range.previous(value);
          final AbstractRange<?> newRange = range.createNew(from, previous);
          iterator.set(newRange);
          this.size--;
          return true;
        } else {
          final Object previous = range.previous(value);
          final AbstractRange<?> newRange1 = range.createNew(from, previous);
          iterator.set(newRange1);

          final Object next = range.next(value);
          final AbstractRange<?> newRange2 = range.createNew(next, to);
          iterator.add(newRange2);
          this.size--;
          return true;
        }
      }
    }

    return false;
  }

  public boolean removeAll(final Iterable<Object> values) {
    boolean removed = false;
    for (final Object value : values) {
      removed |= remove(value);
    }
    return removed;
  }

  @SuppressWarnings("unchecked")
  public <V> V removeFirst() {
    if (size() == 0) {
      return null;
    } else {
      final AbstractRange<?> range = this.ranges.get(0);
      final Object value = range.getFrom();
      remove(value);
      return (V)value;
    }
  }

  public boolean removeRange(final Object from, final Object to) {
    boolean removed = false;
    for (final ListIterator<AbstractRange<?>> iterator = this.ranges.listIterator(); iterator.hasNext();) {
      final AbstractRange<?> range = iterator.next();

      final Object rangeFrom = range.getFrom();
      final Object rangeTo = range.getTo();
      if (range.compareFromValue(from) >= 0) {

        if (range.compareFromValue(to) > 0) {
          return removed;
        } else if (range.compareToValue(to) <= 0) {
          iterator.remove();
        } else {
          final Object next = range.next(to);
          final AbstractRange<?> newRange = range.createNew(next, rangeTo);
          this.size -= range.size();
          this.size += newRange.size();
          iterator.set(newRange);
        }
      } else if (range.compareToValue(from) >= 0) {
        if (range.compareToValue(to) > 0) {
          final Object next = range.next(to);
          final Object previous = range.previous(from);
          final AbstractRange<?> newRange1 = range.createNew(rangeFrom, previous);
          iterator.set(newRange1);
          final AbstractRange<?> newRange2 = range.createNew(next, rangeTo);
          iterator.add(newRange2);
          this.size -= range.size();
          this.size += newRange1.size();
          this.size += newRange2.size();

        } else {
          final Object previous = range.previous(from);
          final AbstractRange<?> newRange = range.createNew(rangeFrom, previous);
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

  public List<Object> toList() {
    return CollectionUtil.list(this);
  }

  @Override
  public String toString() {
    return CollectionUtil.toString(",", this.ranges);
  }
}
