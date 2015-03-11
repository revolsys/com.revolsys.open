package com.revolsys.collection.range;

import java.util.Iterator;
import java.util.List;

import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Numbers;

public abstract class AbstractRange<V> implements Iterable<V>,
  Comparable<AbstractRange<? extends Object>> {

  public static int compare(final Object value1, final Object value2) {
    if (value1 == null) {
      if (value2 == null) {
        return 0;
      } else {
        return -1;
      }
    } else if (value2 == null) {
      return 1;
    } else {
      final Long longValue1 = Numbers.toLong(value1);
      final Long longValue2 = Numbers.toLong(value2);
      if (longValue1 == null) {
        if (longValue2 == null) {
          return value1.toString().compareTo(value2.toString());
        } else {
          return 1;
        }
      } else if (longValue2 == null) {
        return -1;
      } else {
        return Long.compare(longValue1, longValue2);
      }
    }
  }

  public int compareFromValue(final Object value) {
    final V from = getFrom();
    return compare(from, value);
  }

  @Override
  public int compareTo(final AbstractRange<? extends Object> range) {
    final Object rangeFrom = range.getFrom();
    final int fromCompare = compareFromValue(rangeFrom);
    if (fromCompare == 0) {
      final Object rangeTo = range.getTo();
      final int toCompare = compareToValue(rangeTo);
      return toCompare;
    }
    return fromCompare;
  }

  public int compareToValue(final Object value) {
    final V to = getTo();
    return compare(to, value);
  }

  public boolean contains(final Object value) {
    final int fromCompare = compareFromValue(value);
    if (fromCompare <= 0) {
      final int toCompare = compareToValue(value);
      if (toCompare >= 0) {
        return true;
      }
    }
    return false;
  }

  protected AbstractRange<?> createNew(final Object from, final Object to) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    } else if (other == null) {
      return false;
    } else if (other instanceof AbstractRange) {
      final AbstractRange<?> range = (AbstractRange<?>)other;
      final V from = getFrom();
      final Object rangeFrom = range.getFrom();
      if (!EqualsRegistry.equal(from, rangeFrom)) {
        return false;
      } else if (!EqualsRegistry.equal(getTo(), range.getTo())) {
        return false;
      } else {
        return true;
      }
    } else {

      return false;
    }
  }

  /**
   * Create an expanded range if the this range and the other overlap or touch
   *
   * @param range
   * @return
   */
  public AbstractRange<?> expand(final AbstractRange<?> range) {
    final V from = getFrom();
    final V to = getTo();
    final Object rangeFrom = range.getFrom();
    final Object rangeTo = range.getTo();

    final int fromCompare = compareFromValue(rangeFrom);
    final int toCompare = compareToValue(rangeTo);
    if (fromCompare == 0) {
      if (toCompare >= 0) {
        return this;
      } else {
        return range;
      }
    } else if (toCompare == 0) {
      if (fromCompare < 0) {
        return this;
      } else {
        return range;
      }
    } else if (fromCompare < 0) {
      if (toCompare > 0) {
        return this;
      } else if (compareToValue(rangeFrom) > 0) {
        return createNew(from, rangeTo);
      } else if (EqualsRegistry.equal(to, previous(rangeFrom))
        || EqualsRegistry.equal(to, rangeFrom)) {
        return createNew(from, rangeTo);
      }
    } else if (fromCompare > 0) {
      if (toCompare < 0) {
        return range;
      } else if (compareFromValue(rangeTo) < 0) {
        return createNew(rangeFrom, to);
      } else if (EqualsRegistry.equal(previous(from), rangeTo)
        || EqualsRegistry.equal(from, rangeTo)) {
        return createNew(rangeFrom, to);
      }
    }
    return null;
  }

  /**
   * Create an expanded range to include the specified value if possible.
   * <ul>
   * <li>If the range contains this value return this instance.</li>
   * <li>If the value = from - 1 return a new range from value-to</li>
   * <li>If the value = to + 1 return a new range from from-value</li>
   * <li>Otherwise return null as it is not a consecutive range</li>
   * @param value
   * @return
   */
  public AbstractRange<?> expand(final Object value) {
    if (value == null || contains(value)) {
      return this;
    } else {
      final V from = getFrom();
      final V to = getTo();
      final V next = next(value);
      if (next == null) {
        return null;
      } else if (compareFromValue(next) == 0) { // value == from -1
        return createNew(value, to);
      } else {
        final V previous = previous(value);
        if (previous == null) {
          return null;
        } else if (compareToValue(previous) == 0) { // value == to + 1
          return createNew(from, value);
        } else {
          return null;
        }
      }
    }
  }

  public abstract V getFrom();

  public abstract V getTo();

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getFrom().hashCode();
    result = prime * result + getTo().hashCode();
    return result;
  }

  @Override
  public Iterator<V> iterator() {
    return new RangeIterator<V>(this);
  }

  public V next(final Object value) {
    return null;
  }

  public V previous(final Object value) {
    return null;
  }

  public long size() {
    return 1;
  }

  public List<V> toList() {
    return CollectionUtil.list(this);
  }

  @Override
  public String toString() {
    final V from = getFrom();
    final V to = getTo();
    if (from.equals(to)) {
      return from.toString();
    } else {
      return from + "~" + to;
    }
  }
}
