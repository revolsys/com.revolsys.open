package com.revolsys.collection.range;

import com.revolsys.util.Numbers;

public class MinMax {
  private int min = Integer.MAX_VALUE;

  private int max = Integer.MIN_VALUE;

  public MinMax() {
  }

  public MinMax(final int number) {
    add(number);
  }

  public MinMax(final int... numbers) {
    for (final int number : numbers) {
      add(number);
    }
  }

  public MinMax(final int min, final int max) {
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
    if (number < this.min) {
      this.min = number;
      updated = true;
    }
    if (number > this.max) {
      this.max = number;
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
    this.min = Integer.MAX_VALUE;
    this.max = Integer.MIN_VALUE;
  }

  public boolean contains(final int number) {
    return number >= this.min && number <= this.max;
  }

  public boolean contains(final int min, final int max) {
    if (min > max) {
      return contains(max, min);
    } else {
      return min >= this.min && max <= this.max;
    }
  }

  public int getMax() {
    return this.max;
  }

  public int getMin() {
    return this.min;
  }

  public boolean isEmpty() {
    return this.min == Integer.MAX_VALUE;
  }

  public boolean overlaps(final int min, final int max) {
    return Numbers.overlaps(min, max, this.min, this.max);
  }

  @Override
  public String toString() {
    if (isEmpty()) {
      return "EMPTY";
    } else {
      return this.min + "-" + this.max;
    }
  }
}
