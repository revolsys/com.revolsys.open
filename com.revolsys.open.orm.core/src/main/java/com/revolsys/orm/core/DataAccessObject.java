package com.revolsys.orm.core;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface DataAccessObject<T> {
  void clearCache();

  ResultPager page(final Map<String, Object> where,
    final Map<String, Boolean> orderBy);

  <V> List<V> list(final String propertyName, final Map<String, Object> where,
    final Map<String, Boolean> orderBy);

  void evict(T object);

  void flush();

  T load(long id);

  T loadAndLock(long id);

  void lock(T object);

  void lockAndRefresh(T object);

  void merge(T object);

  void persist(T object);

  void refresh(T object);

  void remove(T object);

  void removeAll(Collection<T> objects);

  T createInstance();

  List<String> list(String propertyName, Map<String, Object> where,
    Map<String, Boolean> order, int limit);
}
