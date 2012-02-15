package com.revolsys.io.page;

public interface PageValueManager<T> {
  void disposeBytes(final byte[] bytes);

  byte[] getBytes(final Page page);

  byte[] getBytes(final T value);

  <V extends T> V getValue(final byte[] bytes);

  <V extends T> V readFromPage(final Page page);
}
