package com.revolsys.gis.model.geometry.base;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.GeometryCollection;

public abstract class BaseGeometry implements Geometry {
  public double distance(
    GeometryCollection<?> geometryCollection) {
    double minDistance = Double.MAX_VALUE;
    for (Geometry geometry : geometryCollection) {
      double distance = distance(geometry);
      if (distance < minDistance) {
        minDistance = distance;
      }
    }
    return minDistance;
  }
}
