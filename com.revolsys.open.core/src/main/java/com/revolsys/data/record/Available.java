package com.revolsys.data.record;

public interface Available {
  default boolean isAvailable() {
    return true;
  }
}
