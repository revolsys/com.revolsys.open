package com.revolsys.geometry.model;

public enum ClockDirection {
  CLOCKWISE, //
  COUNTER_CLOCKWISE;

  public static final ClockDirection OGC_SFS_COUNTER_CLOCKWISE = COUNTER_CLOCKWISE;

  public boolean isClockwise() {
    return this == CLOCKWISE;
  }

  public boolean isCounterClockwise() {
    return this == COUNTER_CLOCKWISE;
  }

  public ClockDirection opposite() {
    if (this == CLOCKWISE) {
      return COUNTER_CLOCKWISE;
    } else {
      return CLOCKWISE;
    }
  }
}
