package com.revolsys.geometry.cs.projection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.Projection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

public final class ProjectionFactory {
  /** The map from projection names to projection classes. */
  private static final Map<String, Class<? extends CoordinatesProjection>> projectionClasses = new HashMap<>();

  static {
    registerCoordinatesProjection(Projection.ALBERS_EQUAL_AREA, AlbersConicEqualArea.class);
    registerCoordinatesProjection(Projection.TRANSVERSE_MERCATOR, TransverseMercator.class);
    registerCoordinatesProjection(Projection.MERCATOR, Mercator1SP.class);
    registerCoordinatesProjection(Projection.POPULAR_VISUALISATION_PSEUDO_MERCATOR,
      WebMercator.class);
    registerCoordinatesProjection(Projection.MERCATOR_1SP, Mercator1SP.class);
    registerCoordinatesProjection(Projection.MERCATOR_2SP, Mercator2SP.class);
    registerCoordinatesProjection(Projection.MERCATOR_1SP_SPHERICAL, Mercator1SPSpherical.class);
    registerCoordinatesProjection(Projection.LAMBERT_CONIC_CONFORMAL_1SP,
      LambertConicConformal1SP.class);
    registerCoordinatesProjection(Projection.LAMBERT_CONIC_CONFORMAL_2SP,
      LambertConicConformal.class);
    registerCoordinatesProjection(Projection.LAMBERT_CONIC_CONFORMAL_2SP_BELGIUM,
      LambertConicConformal.class);
  }

  public static Point convert(final Point point, final GeometryFactory sourceGeometryFactory,
    final GeometryFactory targetGeometryFactory) {
    if (point == null) {
      return null;
    } else if (sourceGeometryFactory == targetGeometryFactory) {
      return point;
    } else if (sourceGeometryFactory == null) {
      return point;
    } else if (targetGeometryFactory == null) {
      return point;
    } else {
      return sourceGeometryFactory.point(point).convertGeometry(targetGeometryFactory);
    }
  }

  public static CoordinatesOperation getCoordinatesOperation(
    final CoordinateSystem fromCoordinateSystem, final CoordinateSystem toCoordinateSystem) {
    if (fromCoordinateSystem == null) {
      return null;
    } else {
      return fromCoordinateSystem.getCoordinatesOperation(toCoordinateSystem);
    }
  }

  public static CoordinatesProjection newCoordinatesProjection(
    final ProjectedCoordinateSystem coordinateSystem) {
    final Projection projection = coordinateSystem.getProjection();
    final String projectionName = projection.getNormalizedName();
    synchronized (projectionClasses) {
      final Class<? extends CoordinatesProjection> projectionClass = projectionClasses
        .get(projectionName);
      if (projectionClass == null) {
        return null;
      } else {
        try {
          final Constructor<? extends CoordinatesProjection> constructor = projectionClass
            .getConstructor(ProjectedCoordinateSystem.class);
          final CoordinatesProjection coordinateProjection = constructor
            .newInstance(coordinateSystem);
          return coordinateProjection;
        } catch (final NoSuchMethodException e) {
          throw new IllegalArgumentException("Constructor " + projectionClass + "("
            + ProjectedCoordinateSystem.class.getName() + ") does not exist");
        } catch (final InstantiationException e) {
          throw new IllegalArgumentException(projectionClass + " cannot be instantiated", e);
        } catch (final IllegalAccessException e) {
          throw new IllegalArgumentException(projectionClass + " cannot be instantiated", e);
        } catch (final InvocationTargetException e) {
          final Throwable cause = e.getCause();
          if (cause instanceof RuntimeException) {
            throw (RuntimeException)cause;
          } else if (cause instanceof Error) {
            throw (Error)cause;
          } else {
            throw new IllegalArgumentException(projectionClass + " cannot be instantiated", cause);
          }
        }
      }
    }
  }

  /**
   * Register a projection for the named projection.
   *
   * @param name The name.
   * @param projectionClass The projection class.
   */
  public static void registerCoordinatesProjection(final String name,
    final Class<? extends CoordinatesProjection> projectionClass) {
    projectionClasses.put(name, projectionClass);
  }

  private ProjectionFactory() {
  }
}
