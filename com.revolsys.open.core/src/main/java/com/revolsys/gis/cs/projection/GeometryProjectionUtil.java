package com.revolsys.gis.cs.projection;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.vividsolutions.jts.geom.Geometry;

public class GeometryProjectionUtil {
  public static CoordinateSystem getCoordinateSystem(final Geometry geometry) {
    final com.vividsolutions.jts.geom.GeometryFactory geometryFactory = GeometryFactory.getFactory(geometry);
    if (geometryFactory instanceof GeometryFactory) {
      final GeometryFactory factory = (GeometryFactory)geometryFactory;
      return factory.getCoordinateSystem();
    } else {
      return EpsgCoordinateSystems.getCoordinateSystem(geometry.getSRID());
    }
  }

  public static GeometryOperation getGeometryOperation(
    final int fromSrid,
    final CoordinateSystem toCoordinateSystem) {
    if (fromSrid == 0 || toCoordinateSystem == null) {
      return null;
    } else {
      final CoordinateSystem fromCoordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(fromSrid);
      return ProjectionFactory.getGeometryOperation(fromCoordinateSystem,
        toCoordinateSystem);
    }
  }

  public static GeometryOperation getGeometryOperation(
    final int fromSrid,
    final GeometryFactory toGeometryFactory) {
    if (fromSrid == 0) {
      return null;
    } else {
      final CoordinateSystem fromCoordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(fromSrid);
      return ProjectionFactory.getGeometryOperation(fromCoordinateSystem,
        toGeometryFactory);
    }
  }

  public static GeometryOperation getGeometryOperation(
    final int sourceSrid,
    final int targetSrid) {
    if (sourceSrid == 0 || targetSrid == 0) {
      return null;
    } else {
      final CoordinateSystem toCoordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(targetSrid);
      return getGeometryOperation(sourceSrid, toCoordinateSystem);
    }
  }

  public static <T extends Geometry> T perform(
    final GeometryOperation operation,
    final T geometry) {
    if (operation == null) {
      return geometry;
    } else {
      return operation.perform(geometry);
    }
  }

  public static <T extends Geometry> T perform(
    final T geometry,
    final CoordinateSystem coordinateSystem) {
    final int geometrySrid = geometry.getSRID();
    if (geometrySrid == 0) {
      return geometry;
    } else {
      final GeometryOperation operation = getGeometryOperation(geometrySrid,
        coordinateSystem);
      return perform(operation, geometry);
    }
  }

  public static <T extends Geometry> T perform(
    final T geometry,
    final CoordinateSystem fromCoordinateSystem,
    final CoordinateSystem toCoordinateSystem) {
    if (fromCoordinateSystem == null || toCoordinateSystem == null) {
      return geometry;
    } else {
      final GeometryOperation operation = ProjectionFactory.getGeometryOperation(
        fromCoordinateSystem, toCoordinateSystem);
      return perform(operation, geometry);
    }
  }

  public static <T extends Geometry> T perform(
    final T geometry,
    final GeometryFactory geometryFactory) {
    if (geometry == null) {
      return null;
    } else {
      final int geometrySrid = geometry.getSRID();
      if (geometrySrid == 0) {
        return geometry;
      } else {
        final GeometryOperation operation = getGeometryOperation(geometrySrid,
          geometryFactory);
        T newGeometry = perform(operation, geometry);
        if (geometry == newGeometry) {
          newGeometry = (T)geometryFactory.createGeometry(geometry);
        }
        return newGeometry;
      }
    }
  }

  public static <T extends Geometry> T perform(final T geometry, final int srid) {
    final int geometrySrid = geometry.getSRID();
    if (geometrySrid == 0) {
      return geometry;
    } else {
      final GeometryOperation operation = getGeometryOperation(geometrySrid,
        srid);
      return perform(operation, geometry);
    }
  }
}
