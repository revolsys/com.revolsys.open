package com.revolsys.geometry.cs;

import java.util.Arrays;
import java.util.List;

public interface UnitOfMeasure {
  static List<String> TYPE_NAMES = Arrays.asList("scale", "length", "angle", "time");

  double toBase(final double value);
}
