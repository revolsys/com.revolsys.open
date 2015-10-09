package com.revolsys.geometry.model.util;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.util.GeometryMapper.MapOp;

public class GeometryIntersectionMapOp implements MapOp {
  private final Geometry geometry;

  public GeometryIntersectionMapOp(final Geometry geometry) {
    this.geometry = geometry;
  }

  @Override
  public Geometry map(final Geometry geometry) {
    return geometry.intersection(this.geometry);
  }
}
