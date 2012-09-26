package com.revolsys.gis.model.geometry.impl;

import java.util.Collection;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.MultiLineString;

public class MultiLineStringImpl extends GeometryCollectionImpl implements
  MultiLineString {

  protected MultiLineStringImpl(GeometryFactoryImpl geometryFactory,
    Collection<? extends Geometry> geometries) {
    super(geometryFactory, LineString.class, geometries);
  }

  public MultiLineStringImpl(GeometryFactoryImpl geometryFactory,
    Class<? extends LineString> geometryClass, Collection<? extends Geometry> geometries) {
    super(geometryFactory, geometryClass, geometries);
  }

  @Override
  public MultiLineStringImpl clone() {
    return (MultiLineStringImpl)super.clone();
  }
}
