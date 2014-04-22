package com.revolsys.jtstest.util;

import com.revolsys.jts.geom.Geometry;

public class GeometryDataUtil {
  public static void setComponentDataToIndex(final Geometry geom) {
    for (int i = 0; i < geom.getGeometryCount(); i++) {
      final Geometry comp = geom.getGeometry(i);
      comp.setUserData("Component # " + i);
    }
  }
}
