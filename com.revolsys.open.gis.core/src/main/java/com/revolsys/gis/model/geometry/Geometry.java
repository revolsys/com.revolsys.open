package com.revolsys.gis.model.geometry;

public interface Geometry {
  double distance(
    Geometry geometry);

  double distance(
    GeometryCollection<?> geometryCollection);
}
