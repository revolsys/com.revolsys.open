package com.revolsys.collection.range;

import com.revolsys.util.number.Integers;

/**
 *
 * Ranges are immutable
 */
public class IntRange extends AbstractRange<Integer> {
  private int from;

  private int to;

  public IntRange() {
  }

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
  public AbstractRange<?> expand(final Object value) {
    final Integer intValue = Integers.toInteger(value);
    if (intValue == null) {
      return null;
    } else {
      return super.expand(intValue);
    }
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
  protected IntRange newRange(final Object from, final Object to) {
    return new IntRange((Integer)from, (Integer)to);
  }

  @Override
  public Integer next(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Integer intValue = Integers.toInteger(value);
      if (intValue == null) {
        return null;
      } else {
        final int number = intValue.intValue();
        if (number == Integer.MAX_VALUE) {
          return null;
        } else {
          return number + 1;
        }
      }
    }
  }

  @Override
  public Integer previous(final Object value) {
    if (value == null) {
      return null;
    } else {
      final Integer intValue = Integers.toInteger(value);
      if (intValue == null) {
        return null;
      } else {
        final int number = intValue.intValue();
        if (number == Integer.MIN_VALUE) {
          return null;
        } else {
          return number - 1;
        }
      }
    }
  }

  protected void setFrom(final int from) {
    this.from = from;
  }

  protected void setTo(final int to) {
    this.to = to;
  }

  @Override
  public long size() {
    return this.to - this.from + 1;
  }
}
