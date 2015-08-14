package com.revolsys.collection.range;

import com.revolsys.util.Emptyable;
import com.revolsys.util.Numbers;
import com.revolsys.util.Parity;
import com.revolsys.util.Property;

public class MinMax extends IntRange implements Emptyable {

  public MinMax() {
    setFrom(Integer.MAX_VALUE);
    setTo(Integer.MIN_VALUE);
  }

  public MinMax(final int number) {
    super(number, number);
  }

  public MinMax(final int... numbers) {
    this();
    for (final int number : numbers) {
      add(number);
    }
  }

  public MinMax(final int min, final int max) {
    this();
    add(min);
    add(max);
  }

  /**
   * Add the number
   * @param number
   * @return True if the min or max was updated.
   */
  public boolean add(final int number) {
    boolean updated = false;
    if (number < getMin()) {
      setFrom(number);
      updated = true;
    }
    if (number > getMax()) {
      setTo(number);
      updated = true;
    }
    return updated;
  }

  public boolean add(final Number number) {
    if (number == null) {
      return false;
    } else {
      return add(number.intValue());
    }
  }

  public void clear() {
    setFrom(Integer.MAX_VALUE);

    setTo(Integer.MIN_VALUE);
  }

  public MinMax clip(int min, int max) {
    if (min > max) {
      return clip(max, min);
    } else {
      if (min > getMax() || getMin() > max) {
        return new MinMax();
      } else {
        if (min < getMin()) {
          min = getMin();
        }
        if (max > getMax()) {
          max = getMax();
        }
        return new MinMax(min, max);
      }
    }
  }

  public MinMax clip(final MinMax minMax) {
    if (isEmpty() || minMax.isEmpty()) {
      return new MinMax();
    } else {
      final int min = minMax.getMin();
      final int max = minMax.getMax();
      return clip(min, max);
    }
  }

  public MinMax clip(MinMax minMax, final Parity parity) {
    minMax = clip(minMax);
    if (Property.hasValue(minMax)) {
      Boolean even = null;
      if (Parity.isEven(parity)) {
        even = true;
      } else if (Parity.isOdd(parity)) {
        even = false;
      }
      if (even != null) {
        boolean changed = false;
        int min = minMax.getMin();
        int max = minMax.getMax();
        if (Numbers.isEven(min) != even) {
          min = min + 1;
          changed = true;
        }
        if (Numbers.isEven(max) != even) {
          max = max - 1;
          changed = true;
        }
        if (changed) {
          if (min > max) {
            return new MinMax();
          } else {
            minMax = new MinMax(min, max);
          }
        }
      }
    }
    return minMax;
  }

  public boolean contains(final int number) {
    return number >= getMin() && number <= getMax();
  }

  public boolean contains(final int min, final int max) {
    if (min > max) {
      return contains(max, min);
    } else {
      return min >= getMin() && max <= getMax();
    }
  }

  public boolean contains(final MinMax minMax) {
    if (isEmpty() || !Property.hasValue(minMax)) {
      return false;
    } else {
      final int min = minMax.getMin();
      final int max = minMax.getMax();
      return contains(min, max);
    }
  }

  @Override
  public boolean equals(final Object object) {
    if (object == this) {
      return true;
    } else if (object instanceof MinMax) {
      final MinMax minMax = (MinMax)object;
      if (getMin() == minMax.getMin()) {
        if (getMax() == minMax.getMax()) {
          return true;
        }
      }
    }
    return false;
  }

  public int getMax() {
    return getTo();
  }

  public int getMin() {
    return getFrom();
  }

  @Override
  public int hashCode() {
    if (isEmpty()) {
      return Integer.MAX_VALUE;
    } else {
      return 31 * getMin() + getMax();
    }
  }

  @Override
  public boolean isEmpty() {
    return getMin() == Integer.MAX_VALUE;
  }

  public boolean overlaps(final int min, final int max) {
    return Numbers.overlaps(getMin(), getMax(), min, max);
  }

  public boolean overlaps(final MinMax minMax) {
    if (isEmpty() || !Property.hasValue(minMax)) {
      return false;
    } else {
      final int min = minMax.getMin();
      final int max = minMax.getMax();
      return overlaps(min, max);
    }
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "EMPTY";
    } else {
      return getMin() + "-" + getMax();
    }
  }
}
