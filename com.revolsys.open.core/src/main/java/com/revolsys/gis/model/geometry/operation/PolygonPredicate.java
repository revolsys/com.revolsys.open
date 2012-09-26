package com.revolsys.gis.model.geometry.operation;

import com.revolsys.gis.model.geometry.Polygon;

public abstract class PolygonPredicate {
  protected Polygon polygon;

  public PolygonPredicate(final Polygon polygon) {
    this.polygon = polygon;
  }

}
