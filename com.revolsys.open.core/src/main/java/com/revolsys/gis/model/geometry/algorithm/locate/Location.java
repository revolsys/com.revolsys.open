package com.revolsys.gis.model.geometry.algorithm.locate;

public enum Location {
  NONE(-1), INTERIOR(0), BOUNDARY(1), EXTERIOR(2);

  private int index;

  private Location(final int index) {
    this.index = index;
  }

  public int getIndex() {
    return index;
  }
}
