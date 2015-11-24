package com.revolsys.identifier;

import java.util.Collections;
import java.util.List;

import com.revolsys.equals.Equals;
import com.revolsys.util.number.Integers;

public final class IntegerIdentifier extends Number implements Identifier, Comparable<Object> {
  private static final long serialVersionUID = 1L;

  private final int value;

  IntegerIdentifier(final int value) {
    this.value = value;
  }

  @Override
  public int compareTo(final Object object) {
    int intValue;
    if (object instanceof Number) {
      final Number number = (Number)object;
      intValue = number.intValue();
    } else {
      final Integer integer = Integers.toInteger(object);
      if (integer == null) {
        return -1;
      } else {
        intValue = integer;
      }
    }
    return Integer.compare(this.value, intValue);
  }

  @Override
  public double doubleValue() {
    return this.value;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Number) {
      final Number number = (Number)other;
      return this.value == number.intValue();
    } else if (other instanceof Identifier) {
      final Identifier identifier = (Identifier)other;
      final List<Object> values = identifier.getValues();
      if (values.size() == 1) {
        final Object otherValue = values.get(0);
        return Equals.equal(this.value, otherValue);
      } else {
        return false;
      }
    } else {
      return Equals.equal(this.value, other);
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
      return (V)Integer.valueOf(this.value);
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
    return this.value;
  }

  @Override
  public int intValue() {
    return this.value;
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
    return Integer.toString(this.value);
  }
}
