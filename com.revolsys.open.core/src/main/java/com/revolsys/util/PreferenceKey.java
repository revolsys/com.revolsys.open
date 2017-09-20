package com.revolsys.util;

public class PreferenceKey {

  private final String path;

  private final String name;

  public PreferenceKey(final String path, final String name) {
    this.path = path;
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public String getPath() {
    return this.path;
  }

  @Override
  public String toString() {
    return this.path + ":" + this.name;
  }
}
