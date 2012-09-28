package com.revolsys.gis.model.geometry.impl;

import java.util.Collection;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.MultiPoint;
import com.revolsys.gis.model.geometry.Point;

public class MultiPointImpl extends GeometryCollectionImpl implements
  MultiPoint {

  protected MultiPointImpl(final GeometryFactoryImpl geometryFactory,
    final Collection<? extends Geometry> geometries) {
    super(geometryFactory, Point.class, geometries);
  }

  @Override
  public MultiPointImpl clone() {
    return (MultiPointImpl)super.clone();
  }
}
