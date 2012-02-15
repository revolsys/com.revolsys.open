package com.revolsys.gis.graph.attribute;

public interface ObjectAttributeProxy<T, O> {
  void clearValue();

  T getValue(final O object);
}
