package com.revolsys.geometry.cs.unit;

import java.util.Arrays;
import java.util.List;

public interface UnitOfMeasure {
  static List<String> TYPE_NAMES = Arrays.asList("scale", "length", "angle", "time");

  UnitOfMeasureType getType();

  double toBase(final double value);

  default double toNormal(final double value) {
    return toBase(value);
  }
}
