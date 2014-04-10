package com.revolsys.gis.model.geometry.impl;

import com.revolsys.gis.model.geometry.LinearRing;
import com.revolsys.jts.geom.CoordinatesList;

public class LinearRingImpl extends LineStringImpl implements LinearRing {

  private static final long serialVersionUID = 1L;

  public LinearRingImpl(final GeometryFactoryImpl geometryFactory,
    final CoordinatesList points) {
    super(geometryFactory, points);
    // TODO check closed and simple
  }

}
