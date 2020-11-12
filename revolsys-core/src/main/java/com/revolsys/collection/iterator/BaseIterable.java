package com.revolsys.collection.iterator;

import java.util.List;

import com.revolsys.collection.list.Lists;

public interface BaseIterable<T> extends Iterable<T> {

  default List<T> asList() {
    return Lists.toArray(this);
  }
}
