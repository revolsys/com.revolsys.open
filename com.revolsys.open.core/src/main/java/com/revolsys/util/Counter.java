package com.revolsys.util;

public interface Counter {
  long add();

  long add(final long count);

  long add(final Number count);

  long get();

  String getName();
}
