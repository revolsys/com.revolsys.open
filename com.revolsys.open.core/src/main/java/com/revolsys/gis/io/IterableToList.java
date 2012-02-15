package com.revolsys.gis.io;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.FactoryBean;

public class IterableToList<T> implements FactoryBean {
  private Iterable<T> iterable;

  private List<T> list;

  public IterableToList() {
  }

  public IterableToList(final Iterable<T> iterable) {
    this.iterable = iterable;
  }

  public Iterable<T> getIterable() {
    return iterable;
  }

  private List<T> getList() {
    if (list == null) {
      list = new ArrayList<T>();
      for (final T value : iterable) {
        list.add(value);
      }
    }
    return list;
  }

  public Object getObject() throws Exception {
    return getList();
  }

  public Class getObjectType() {
    return List.class;
  }

  public boolean isSingleton() {
    return true;
  }

  public void setIterable(final Iterable<T> iterable) {
    this.iterable = iterable;
  }

}
