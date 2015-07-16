package com.revolsys.predicate;

import java.util.Collection;
import java.util.function.Predicate;

import org.apache.commons.beanutils.PropertyUtils;

public class ObjectPropertyValuesPredicate<T> implements Predicate<T> {
  private final String propertyName;

  private final Collection<? extends Object> values;

  public ObjectPropertyValuesPredicate(final String propertyName,
    final Collection<? extends Object> values) {
    this.propertyName = propertyName;
    this.values = values;
  }

  @Override
  public boolean test(final T object) {
    try {
      final Object value = PropertyUtils.getProperty(object, this.propertyName);
      return this.values.contains(value);
    } catch (final Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
