package com.revolsys.gis.data.io;

import java.io.InputStream;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.ResultPager;

public interface DataAccessObject<T> {
  void clearCache();

  Blob createBlob(byte[] bytes);

  Blob createBlob(InputStream stream, long length);

  Clob createClob(String string);

  T createInstance();

  void evict(T object);

  void flush();

  List<T> list(Map<String, Object> filter, Map<String, Boolean> orderBy);

  <V> List<V> list(final String propertyName, final Map<String, Object> where,
    final Map<String, Boolean> orderBy);

  List<String> list(String propertyName, Map<String, Object> where,
    Map<String, Boolean> order, int limit);

  T load(long id);

  T loadAndLock(long id);

  void lock(T object);

  void lockAndRefresh(T object);

  void merge(T object);

  ResultPager<T> page(final Map<String, Object> where,
    final Map<String, Boolean> orderBy);

  void persist(T object);

  void refresh(T object);

  void remove(T object);

  void removeAll(Collection<T> objects);
}
