package com.revolsys.filter;

public interface Factory<T, V> {
  T create(V object);
}
