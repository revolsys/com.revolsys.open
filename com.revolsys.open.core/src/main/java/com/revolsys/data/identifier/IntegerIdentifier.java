package com.revolsys.data.identifier;

import java.util.Collections;
import java.util.List;

import com.revolsys.data.equals.Equals;

public final class IntegerIdentifier extends AbstractIdentifier {

  private final int value;

  protected IntegerIdentifier(final int value) {
    this.value = value;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof IntegerIdentifier) {
      final IntegerIdentifier identifier = (IntegerIdentifier)other;
      return this.value == identifier.getIntValue();
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

  public int getIntValue() {
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
  public String toString() {
    return Integer.toString(this.value);
  }
}
