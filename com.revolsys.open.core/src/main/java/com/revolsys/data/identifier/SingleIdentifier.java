package com.revolsys.data.identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;

public class SingleIdentifier extends AbstractIdentifier {

  public static Identifier create(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof Identifier) {
      return (Identifier)value;
    } else if (value instanceof Collection) {
      final Collection<?> idValues = (Collection<?>)value;
      return new ListIdentifier(idValues);
    } else {
      return new SingleIdentifier(value);
    }
  }

  private final Object value;

  protected SingleIdentifier(final Object value) {
    this.value = value;
  }

  @Override
  public List<Object> getValues() {
    return Collections.singletonList(this.value);
  }

  @Override
  public String toString() {
    return StringConverterRegistry.toString(this.value);
  }
}
