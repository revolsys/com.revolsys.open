package com.revolsys.data.identifier;

import java.util.Collections;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.equals.EqualsRegistry;

public class SingleIdentifier extends AbstractIdentifier {

  private final Object value;

  protected SingleIdentifier(final Object value) {
    this.value = value;
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof Identifier) {
      final Identifier identifier = (Identifier)other;
      final List<Object> values = identifier.getValues();
      if (values.size() == 1) {
        final Object otherValue = values.get(0);
        return EqualsRegistry.equal(this.value, otherValue);
      } else {
        return false;
      }
    } else {
      return EqualsRegistry.equal(this.value, other);
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
  public String toString() {
    return StringConverterRegistry.toString(this.value);
  }
}
