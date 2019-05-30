package com.revolsys.identifier;

import java.util.Collections;
import java.util.List;

import org.jeometry.common.data.type.DataType;
import org.jeometry.common.number.Integers;

public final class LongIdentifier extends Number implements Identifier, Comparable<Object> {
  private static final long serialVersionUID = 1L;

  private final long value;

  LongIdentifier(final long value) {
    this.value = value;
  }

  @Override
  public int compareTo(final Object object) {
    long longValue;
    if (object instanceof Number) {
      final Number number = (Number)object;
      longValue = number.longValue();
    } else {
      final Integer longeger = Integers.toInteger(object);
      if (longeger == null) {
        return -1;
      } else {
        longValue = longeger;
      }
    }
    return Long.compare(this.value, longValue);
  }

  @Override
  public double doubleValue() {
    return this.value;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Number) {
      final Number number = (Number)other;
      return this.value == number.longValue();
    } else if (other instanceof Identifier) {
      final Identifier identifier = (Identifier)other;
      final List<Object> values = identifier.getValues();
      if (values.size() == 1) {
        final Object otherValue = values.get(0);
        return DataType.equal(this.value, otherValue);
      } else {
        return false;
      }
    } else {
      return DataType.equal(this.value, other);
    }
  }

  @Override
  public float floatValue() {
    return this.value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final int index) {
    if (index == 0) {
      return (V)Long.valueOf(this.value);
    } else {
      return null;
    }
  }

  @Override
  public List<Object> getValues() {
    return Collections.singletonList((Object)this.value);
  }

  @Override
  public int hashCode() {
    return Long.hashCode(this.value);
  }

  @Override
  public int intValue() {
    return (int)this.value;
  }

  @Override
  public boolean isSingle() {
    return true;
  }

  @Override
  public long longValue() {
    return this.value;
  }

  @Override
  public String toString() {
    return Long.toString(this.value);
  }
}
