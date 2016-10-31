package com.revolsys.geometry.model;

import com.revolsys.geometry.cs.CoordinateSystem;

public interface GeometryFactoryProxy {
  default BoundingBox convertBoundingBox(final BoundingBoxProxy boundingBoxProxy) {
    if (boundingBoxProxy != null) {
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      if (boundingBox != null) {

        final GeometryFactory geometryFactory = getGeometryFactory();
        if (geometryFactory != null) {
          return boundingBox.convert(geometryFactory);
        }
      }
      return boundingBox;
    }
    return null;
  }

  default <G extends Geometry> G convertGeometry(final G geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryFactory != null) {
        return geometry.convertGeometry(geometryFactory);
      }
    }
    return geometry;
  }

  default <G extends Geometry> G convertGeometry(final G geometry, final int axisCount) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryFactory != null) {
        return geometry.convertGeometry(geometryFactory, axisCount);
      }
    }
    return geometry;
  }

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
    return GeometryFactory.DEFAULT_3D;
  }

  default boolean isSameCoordinateSystem(final GeometryFactory geometryFactory) {
    final GeometryFactory geometryFactory2 = getGeometryFactory();
    if (geometryFactory == null || geometryFactory2 == null) {
      return false;
    } else {
      return geometryFactory.isSameCoordinateSystem(geometryFactory2);
    }
  }

  default boolean isSameCoordinateSystem(final GeometryFactoryProxy proxy) {
    if (proxy == null) {
      return false;
    } else {
      final GeometryFactory geometryFactory = proxy.getGeometryFactory();
      return isSameCoordinateSystem(geometryFactory);
    }
  }
}
