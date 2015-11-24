package com.revolsys.identifier;

import java.util.Collections;
import java.util.List;

import com.revolsys.datatype.DataTypes;
import com.revolsys.equals.Equals;
import com.revolsys.util.CompareUtil;

public class SingleIdentifier implements Identifier, Comparable<Object> {
  private final Object value;

  protected SingleIdentifier(final Object value) {
    this.value = value;
  }

  @Override
  public int compareTo(final Object object) {
    Object otherValue;
    if (object instanceof Identifier) {
      final Identifier identifier = (Identifier)object;
      if (identifier.isSingle()) {
        otherValue = identifier.getValue(0);
      } else {
        return -1;
      }
    } else {
      otherValue = object;
    }
    return CompareUtil.compare(this.value, otherValue);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Identifier) {
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
  public List<Object> getValues() {
    return Collections.singletonList(this.value);
  }

  @Override
  public int hashCode() {
    if (this.value == null) {
      return 0;
    } else {
      return this.value.hashCode();
    }
  }

  @Override
  public boolean isSingle() {
    return true;
  }

  @Override
  public String toString() {
    return DataTypes.toString(this.value);
  }
}
