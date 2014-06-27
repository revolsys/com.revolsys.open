package com.revolsys.gis.data.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.revolsys.converter.string.StringConverterRegistry;

public class SingleRecordIdentifier extends AbstractRecordIdentifier {

  public static RecordIdentifier create(final Object value) {
    if (value == null) {
      return null;
    } else if (value instanceof RecordIdentifier) {
      return (RecordIdentifier)value;
    } else if (value instanceof Collection) {
      final Collection<?> idValues = (Collection<?>)value;
      return new ListRecordIdentifier(idValues);
    } else {
      return new SingleRecordIdentifier(value);
    }
  }

  private final Object value;

  private SingleRecordIdentifier(final Object value) {
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
