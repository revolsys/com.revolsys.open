package com.revolsys.gis.model.geometry.impl;

import java.util.Collection;

import com.revolsys.gis.model.geometry.Geometry;
import com.revolsys.gis.model.geometry.LineString;
import com.revolsys.gis.model.geometry.MultiLineString;

public class MultiLineStringImpl extends GeometryCollectionImpl implements
  MultiLineString {

  public MultiLineStringImpl(final GeometryFactoryImpl geometryFactory,
    final Class<? extends LineString> geometryClass,
    final Collection<? extends Geometry> geometries) {
    super(geometryFactory, geometryClass, geometries);
  }

  protected MultiLineStringImpl(final GeometryFactoryImpl geometryFactory,
    final Collection<? extends Geometry> geometries) {
    super(geometryFactory, LineString.class, geometries);
  }

  @Override
  public MultiLineStringImpl clone() {
    return (MultiLineStringImpl)super.clone();
  }
}
