package com.revolsys.jtstest.function;

import com.revolsys.geometry.dissolve.LineDissolver;
import com.revolsys.geometry.model.Geometry;

public class DissolveFunctions {

  public static Geometry dissolve(final Geometry geom) {
    return LineDissolver.dissolve(geom);
  }
}
