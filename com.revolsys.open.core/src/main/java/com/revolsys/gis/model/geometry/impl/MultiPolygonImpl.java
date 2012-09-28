package com.revolsys.gis.model.geometry.impl;

import java.util.Collection;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.MultiPoint;
import com.revolsys.gis.model.geometry.Polygon;

public class MultiPolygonImpl extends GeometryCollectionImpl implements
  MultiPoint {

  protected MultiPolygonImpl(final GeometryFactoryImpl geometryFactory,
    final Collection<? extends Geometry> geometries) {
    super(geometryFactory, Polygon.class, geometries);
  }

  @Override
  public MultiPolygonImpl clone() {
    return (MultiPolygonImpl)super.clone();
  }
}
