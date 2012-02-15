package com.revolsys.filter;

import java.util.Collection;

import org.apache.commons.beanutils.PropertyUtils;

public class ObjectPropertyValuesFilter<T> implements Filter<T> {
  private final String propertyName;

  private final Collection<? extends Object> values;

  public ObjectPropertyValuesFilter(final String propertyName,
    final Collection<? extends Object> values) {
    this.propertyName = propertyName;
    this.values = values;
  }

  public boolean accept(final T object) {
    try {
      final Object value = PropertyUtils.getProperty(object, propertyName);
      return values.contains(value);
    } catch (final Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
