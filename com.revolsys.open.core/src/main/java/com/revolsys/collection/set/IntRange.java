package com.revolsys.collection.set;

/**
 *
 * Ranges are immutable
 */
public class IntRange extends AbstractRange<Integer> {
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

  @Override
  public int compareFromValue(final Integer value) {
    if (this.from < value) {
      return -1;
    } else if (this.from > value) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public int compareToValue(final Integer value) {
    if (this.to < value) {
      return -1;
    } else if (this.to > value) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public AbstractRange<Integer> createNew(final Integer from, final Integer to) {
    return new IntRange(from, to);
  }

  @Override
  public IntRange expand(final AbstractRange<Integer> range) {
    return (IntRange)super.expand(range);
  }

  @Override
  public IntRange expand(final Integer value) {
    return (IntRange)super.expand(value);
  }

  @Override
  public Integer getFrom() {
    return this.from;
  }

  @Override
  public Integer getTo() {
    return this.to;
  }

  @Override
  public Integer next(final Integer value) {
    if (value == null || value == Integer.MAX_VALUE) {
      return null;
    } else {
      return value + 1;
    }
  }

  @Override
  public Integer previous(final Integer value) {
    if (value == null || value == Integer.MIN_VALUE) {
      return null;
    } else {
      return value - 1;
    }
  }

  @Override
  public int size() {
    return this.to - this.from + 1;
  }
}
