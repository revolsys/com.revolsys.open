package com.revolsys.io.page;

public interface PageValueManager<T> {
  public <V extends T> V readFromByteArray(final byte[] bytes);

  <V extends T> V readFromPage(final Page page);
  
  <V extends T> V removeFromPage(final Page page);

  byte[] writeToByteArray(final T value);
}
