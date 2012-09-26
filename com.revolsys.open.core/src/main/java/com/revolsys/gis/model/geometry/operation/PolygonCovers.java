package com.revolsys.gis.model.geometry.operation;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.Polygon;
import com.revolsys.gis.model.geometry.operation.relate.RelateOp;

public class PolygonCovers extends AbstractPolygonContains {

  public PolygonCovers(final Polygon polygon) {
    super(polygon);
    requireSomePointInInterior = false;
  }

  public boolean covers(final Geometry geom) {
    return eval(geom);
  }

  @Override
  protected boolean fullTopologicalPredicate(final Geometry geometry) {
    return RelateOp.relate(polygon, geometry).isCovers();
  }

}
