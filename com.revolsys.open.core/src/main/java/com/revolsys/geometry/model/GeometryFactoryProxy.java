package com.revolsys.geometry.model;

import com.revolsys.geometry.cs.CoordinateSystem;

public interface GeometryFactoryProxy {
  default CoordinateSystem getCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.getCoordinateSystem();
  }

  default int getCoordinateSystemId() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.getSrid();
  }

  default GeometryFactory getGeometryFactory() {
    return GeometryFactory.floating3();
  }

  default int getSrid() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.getSrid();
  }
}
