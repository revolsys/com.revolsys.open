package com.revolsys.io.page;

public interface PageValueManager<T> {
   <V extends T> V getValue(final byte[] bytes);

  <V extends T> V readFromPage(final Page page);
  
  byte[] getBytes(final Page page);
  
  byte[] getBytes(final T value);
}
