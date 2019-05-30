package com.revolsys.grid;

import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.properties.ObjectWithProperties;

public interface Grid extends ObjectWithProperties, BoundingBoxProxy {
  public static double cubicInterpolate(final double a, final double b, final double c,
    final double d, final double t) {
    return b + 0.5 * t * (c - a + t * (2 * a - 5 * b + 4 * c - d + t * (3 * (b - c) + d - a)));
  }
}
