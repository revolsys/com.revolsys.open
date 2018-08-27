package com.revolsys.geometry.model;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.HorizontalCoordinateSystemProxy;
import com.revolsys.geometry.cs.projection.CoordinatesOperation;
import com.revolsys.geometry.cs.projection.ProjectionFactory;

public interface GeometryFactoryProxy extends HorizontalCoordinateSystemProxy {
  default BoundingBox convertBoundingBox(final BoundingBoxProxy boundingBoxProxy) {
    if (boundingBoxProxy != null) {
      final BoundingBox boundingBox = boundingBoxProxy.getBoundingBox();
      if (boundingBox != null) {

        final GeometryFactory geometryFactory = getGeometryFactory();
        if (geometryFactory != null) {
          return boundingBox.toCs(geometryFactory);
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

  default BoundingBox getAreaBoundingBox() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      return geometryFactory.getAreaBoundingBox();
    }
    return BoundingBox.empty();
  }

  default CoordinatesOperation getCoordinatesOperation(final GeometryFactory geometryFactory) {
    if (geometryFactory == null) {
      return null;
    } else {
      final int coordinateSystemId = geometryFactory.getHorizontalCoordinateSystemId();
      final int coordinateSystemIdThis = getHorizontalCoordinateSystemId();
      if (coordinateSystemId == coordinateSystemIdThis) {
        return null;
      } else if (coordinateSystemId == 0 || coordinateSystemIdThis == 0) {
        return null;
      } else {
        final CoordinateSystem coordinateSystemThis = getHorizontalCoordinateSystem();
        final CoordinateSystem coordinateSystem = geometryFactory.getHorizontalCoordinateSystem();
        if (coordinateSystem == coordinateSystemThis) {
          return null;
        } else if (coordinateSystem == null || coordinateSystemThis == null) {
          return null;
        } else if (coordinateSystem.equals(coordinateSystemThis)) {
          return null;
        } else {
          return ProjectionFactory.getCoordinatesOperation(coordinateSystemThis, coordinateSystem);
        }
      }
    }
  }

  default CoordinatesOperation getCoordinatesOperation(final GeometryFactoryProxy geometryFactory) {
    if (geometryFactory == null) {
      return null;
    } else {
      return getCoordinatesOperation(geometryFactory.getGeometryFactory());
    }
  }

  default <C extends CoordinateSystem> C getCoordinateSystem() {
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

  @Override
  default <C extends CoordinateSystem> C getHorizontalCoordinateSystem() {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory == null) {
      return null;
    } else {
      return geometryFactory.getHorizontalCoordinateSystem();
    }
  }

  default GeometryFactory getNonZeroGeometryFactory(GeometryFactory geometryFactory) {
    final GeometryFactory geometryFactoryThis = getGeometryFactory();
    if (geometryFactory == null) {
      return geometryFactoryThis;
    } else {
      final int srid = geometryFactory.getHorizontalCoordinateSystemId();
      if (srid == 0) {
        final int geometrySrid = geometryFactoryThis.getHorizontalCoordinateSystemId();
        if (geometrySrid != 0) {
          geometryFactory = geometryFactory.convertSrid(geometrySrid);
        }
      }
      return geometryFactory;
    }
  }

  default boolean isHasHorizontalCoordinateSystem() {
    final CoordinateSystem coordinateSystem = getHorizontalCoordinateSystem();
    return coordinateSystem != null;
  }

  default boolean isProjectionRequired(final GeometryFactory geometryFactory) {
    final GeometryFactory geometryFactoryThis = getGeometryFactory();
    if (geometryFactoryThis == null) {
      return false;
    } else {
      return geometryFactoryThis.isProjectionRequired(geometryFactory);
    }
  }

  default boolean isProjectionRequired(final GeometryFactoryProxy geometryFactoryProxy) {
    if (geometryFactoryProxy == null) {
      return false;
    } else {
      final GeometryFactory geometryFactory = geometryFactoryProxy.getGeometryFactory();
      return isProjectionRequired(geometryFactory);
    }
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

  default <G extends Geometry> G toCoordinateSystem(final G geometry) {
    if (geometry != null) {
      final GeometryFactory geometryFactory = getGeometryFactory();
      if (geometryFactory != null) {
        if (!geometry.isSameCoordinateSystem(geometryFactory)) {
          return geometry.convertGeometry(geometryFactory);
        }
      }
    }
    return geometry;
  }

  default double toDoubleX(final int x) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toDoubleX(x);
  }

  default double toDoubleY(final int y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toDoubleY(y);
  }

  default double toDoubleZ(final int z) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toDoubleZ(z);
  }

  default int toIntX(final double x) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toIntX(x);
  }

  default int toIntY(final double y) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toIntY(y);
  }

  default int toIntZ(final double z) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    return geometryFactory.toIntZ(z);
  }

  default void writePrjFile(final Object target) {
    final GeometryFactory geometryFactory = getGeometryFactory();
    if (geometryFactory != null) {
      geometryFactory.writePrjFile(target);
    }
  }
}
