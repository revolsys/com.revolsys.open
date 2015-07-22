package com.revolsys.data.identifier;

import java.util.Collections;
import java.util.List;

import com.revolsys.data.equals.Equals;

public final class LongIdentifier extends AbstractIdentifier {

  private final long value;

  protected LongIdentifier(final long value) {
    this.value = value;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof LongIdentifier) {
      final LongIdentifier identifier = (LongIdentifier)other;
      return this.value == identifier.getLongValue();
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

  public long getLongValue() {
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
    return (int)this.value;
  }

  @Override
  public String toString() {
    return Long.toString(this.value);
  }
}
