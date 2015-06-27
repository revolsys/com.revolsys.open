package com.revolsys.gis.graph.filter;

import com.revolsys.data.equals.EqualsInstance;
import com.revolsys.filter.Filter;
import com.revolsys.properties.ObjectWithProperties;

public class AttributeFilter<T extends ObjectWithProperties> implements Filter<T> {
  private final String fieldName;

  private final boolean inverse;

  private final Object value;

  public AttributeFilter(final String fieldName, final Object value) {
    this.fieldName = fieldName;
    this.value = value;
    this.inverse = false;
  }

  public AttributeFilter(final String fieldName, final Object value, final boolean inverse) {
    this.fieldName = fieldName;
    this.value = value;
    this.inverse = inverse;
  }

  @Override
  public boolean accept(final T object) {
    final Object value = object.getProperty(this.fieldName);
    final boolean equal = EqualsInstance.INSTANCE.equals(this.value, value);
    if (this.inverse) {
      return !equal;
    } else {
      return equal;
    }
  }
}
