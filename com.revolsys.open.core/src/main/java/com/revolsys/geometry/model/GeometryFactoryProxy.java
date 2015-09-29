package com.revolsys.geometry.model;

import com.revolsys.geometry.cs.CoordinateSystem;

public interface GeometryFactoryProxy {
  default CoordinateSystem getCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return null;
    } else {
      return geometryFactory.getCoordinateSystem();
    }
  }

  default int getCoordinateSystemId() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return 0;
    } else {
      return geometryFactory.getCoordinateSystemId();
    }
  }

  default String getCoordinateSystemName() {
    final CoordinateSystem coordinateSystem = getCoordinateSystem();
    if (coordinateSystem == null) {
      return "Unknown";
    } else {
      return coordinateSystem.getCoordinateSystemName();
    }
  }

  default GeometryFactory getGeometryFactory() {
    return GeometryFactory.floating3();
  }
}
