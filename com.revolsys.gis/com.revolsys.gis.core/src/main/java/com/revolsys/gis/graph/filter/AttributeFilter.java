package com.revolsys.gis.graph.filter;

import com.revolsys.filter.Filter;
import com.revolsys.gis.graph.AttributedObject;
import com.revolsys.gis.model.data.equals.EqualsRegistry;

public class AttributeFilter<T extends AttributedObject> implements Filter<T> {
  private final String attributeName;

  private final boolean inverse;

  private final Object value;

  public AttributeFilter(
    final String attributeName,
    final Object value) {
    this.attributeName = attributeName;
    this.value = value;
    this.inverse = false;
  }

  public AttributeFilter(
    final String attributeName,
    final Object value,
    final boolean inverse) {
    this.attributeName = attributeName;
    this.value = value;
    this.inverse = inverse;
  }

  public boolean accept(
    final T object) {
    final Object value = object.getAttribute(attributeName);
    final boolean equal = EqualsRegistry.INSTANCE.equals(this.value, value);
    if (inverse) {
      return !equal;
    } else {
      return equal;
    }
  }
}
