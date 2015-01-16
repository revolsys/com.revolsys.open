package com.revolsys.gis.io;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.config.AbstractFactoryBean;

public class IterableToList<T> extends AbstractFactoryBean<List<T>> {
  private Iterable<T> iterable;

  public IterableToList() {
  }

  public IterableToList(final Iterable<T> iterable) {
    this.iterable = iterable;
  }

  @Override
  protected List<T> createInstance() throws Exception {
    final List<T> list = new ArrayList<T>();
    for (final T value : this.iterable) {
      list.add(value);
    }
    return list;
  }

  public Iterable<T> getIterable() {
    return this.iterable;
  }

  @Override
  public Class<?> getObjectType() {
    return List.class;
  }

  public void setIterable(final Iterable<T> iterable) {
    this.iterable = iterable;
  }

}
