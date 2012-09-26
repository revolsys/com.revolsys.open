package com.revolsys.gis.model.geometry;


public interface Polygon extends Geometry, Polygonal, Iterable<LinearRing> {
  LinearRing getExteriorRing();

  LinearRing getRing(int index);

  int getRingCount();

  MultiLinearRing getRings();
}
