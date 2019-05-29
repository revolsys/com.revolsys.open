package com.revolsys.record.io.test;

import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;

@FunctionalInterface
public interface GeometryTestFunction<GF extends GeometryFactory, G extends Geometry, D extends DataType> {
  void apply(GF geometryFactory, G geometry, D dataType);
}
