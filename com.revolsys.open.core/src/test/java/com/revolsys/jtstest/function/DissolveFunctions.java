package com.revolsys.jtstest.function;

import com.revolsys.jts.dissolve.LineDissolver;
import com.revolsys.jts.geom.Geometry;

public class DissolveFunctions {

  public static Geometry dissolve(final Geometry geom) {
    return LineDissolver.dissolve(geom);
  }
}
