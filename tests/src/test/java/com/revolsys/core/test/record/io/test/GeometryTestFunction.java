package com.revolsys.core.test.record.io.test;

import org.jeometry.common.datatype.DataType;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;

@FunctionalInterface
public interface GeometryTestFunction<GF extends GeometryFactory, G extends Geometry, D extends DataType> {
  void apply(GF geometryFactory, G geometry, D dataType);
}
