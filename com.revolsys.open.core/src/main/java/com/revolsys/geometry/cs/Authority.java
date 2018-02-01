package com.revolsys.geometry.cs;

public interface Authority {

  String getCode();

  default int getId() {
    return Integer.parseInt(getCode());
  }

  String getName();
}
