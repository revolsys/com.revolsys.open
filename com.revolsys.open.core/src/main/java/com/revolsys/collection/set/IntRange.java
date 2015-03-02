package com.revolsys.collection.set;

import java.util.Iterator;
import java.util.List;

import com.revolsys.util.CollectionUtil;

/**
 *
 * Ranges are immutable
 */
public class IntRange implements Iterable<Integer>, Comparable<IntRange> {
  private int from;

  private int to;

  public IntRange(final int value) {
    this(value, value);
  }

  public IntRange(final int from, final int to) {
    if (from < to) {
      this.from = from;
      this.to = to;
    } else {
      this.from = to;
      this.to = from;
    }
  }

  public int compareFromValue(final int value) {
    if (this.from < value) {
      return -1;
    } else if (this.from > value) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public int compareTo(final IntRange range) {
    if (this.from < range.from) {
      return -1;
    } else if (this.from > range.from) {
      return 1;
    } else {
      if (this.to < range.to) {
        return -1;
      } else if (this.to > range.to) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  public int compareToValue(final int value) {
    if (this.to < value) {
      return -1;
    } else if (this.to > value) {
      return 1;
    } else {
      return 0;
    }
  }

  public boolean contains(final int value) {
    if (value >= this.from && value <= this.to) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    } else if (other == null) {
      return false;
    } else if (other instanceof IntRange) {
      final IntRange range = (IntRange)other;
      if (this.from != range.from) {
        return false;
      } else if (this.to != range.to) {
        return false;
      } else {
        return true;
      }
    } else {

      return false;
    }
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
  public IntRange expand(final int value) {
    if (contains(value)) {
      return this;
    } else if (value == this.from - 1) {
      return new IntRange(value, this.to);
    } else if (value == this.to + 1) {
      return new IntRange(this.from, value);
    } else {
      return null;
    }
  }

  public IntRange expand(final IntRange range) {
    if (this.from == range.from) {
      if (this.to >= range.to) {
        return this;
      } else {
        return range;
      }
    } else if (this.to == range.to) {
      if (this.from < range.from) {
        return this;
      } else {
        return range;
      }
    } else if (this.from < range.from) {
      if (this.to > range.to) {
        return this;
      } else if (this.to > range.from) {
        return new IntRange(this.from, range.to);
      } else if (this.to == range.from - 1 || this.to == range.from) {
        return new IntRange(this.from, range.to);
      }
    } else if (this.from > range.from) {
      if (this.to < range.to) {
        return range;
      } else if (this.from < range.to) {
        return new IntRange(range.from, this.to);
      } else if (this.from - 1 == range.to || this.from == range.to) {
        return new IntRange(range.from, this.to);
      }
    }
    return null;
  }

  public int getFrom() {
    return this.from;
  }

  public int getTo() {
    return this.to;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + this.from;
    result = prime * result + this.to;
    return result;
  }

  @Override
  public Iterator<Integer> iterator() {
    return new IntRangeIterator(this.from, this.to);
  }

  public int size() {
    return this.to - this.from + 1;
  }

  public List<Integer> toList() {
    return CollectionUtil.list(this);
  }

  @Override
  public String toString() {
    if (this.from == this.to) {
      return Integer.toString(this.from);
    } else {
      return this.from + "-" + this.to;
    }
  }

}
