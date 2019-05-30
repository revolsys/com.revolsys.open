package com.revolsys.geometry.cs.projection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.jeometry.coordinatesystem.operation.ChainedCoordinatesOperation;
import org.jeometry.coordinatesystem.operation.CoordinatesOperation;
import org.jeometry.coordinatesystem.operation.UnitConverstionOperation;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.GeographicCoordinateSystem;
import com.revolsys.geometry.cs.ProjectedCoordinateSystem;
import com.revolsys.geometry.cs.Projection;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;

import si.uom.NonSI;
import tec.uom.se.unit.Units;

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

  public static CoordinatesOperation getCoordinatesOperation(final CoordinateSystem cs1,
    final CoordinateSystem cs2) {
    if (cs1 == null || cs2 == null || cs1 == cs2) {
      return null;
    } else {
      final List<CoordinatesOperation> operations = new ArrayList<>();
      if (cs1 instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem pcs1 = (ProjectedCoordinateSystem)cs1;
        final CoordinatesOperation inverseOperation = getInverseCoordinatesOperation(pcs1);
        if (inverseOperation == null) {
          return null;
        }
        final Unit<Length> linearUnit1 = pcs1.getLengthUnit();
        if (!linearUnit1.equals(Units.METRE)) {
          operations.add(new UnitConverstionOperation(linearUnit1, Units.METRE));
        }
        operations.add(inverseOperation);
      } else if (cs1 instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem gcs1 = (GeographicCoordinateSystem)cs1;
        final Unit<Angle> angularUnit1 = gcs1.getUnit();
        if (cs2 instanceof GeographicCoordinateSystem) {
          final GeographicCoordinateSystem gcs2 = (GeographicCoordinateSystem)cs2;
          // TODO Datum shift
          final Unit<Angle> angularUnit2 = gcs2.getUnit();
          if (!angularUnit1.equals(angularUnit2)) {
            return new UnitConverstionOperation(angularUnit1, angularUnit2, 2);
          } else {
            return null;
          }
        } else {
          if (!angularUnit1.equals(Units.RADIAN)) {
            CoordinatesOperation converstionOperation;
            if (angularUnit1.equals(NonSI.DEGREE_ANGLE)) {
              converstionOperation = DegreesToRadiansOperation.INSTANCE;
            } else {
              converstionOperation = new UnitConverstionOperation(angularUnit1, Units.RADIAN, 2);
            }

            operations.add(converstionOperation);
          }
        }
      } else {
        return null;
      }
      if (cs2 instanceof ProjectedCoordinateSystem) {
        final ProjectedCoordinateSystem pcs2 = (ProjectedCoordinateSystem)cs2;
        final CoordinatesOperation projectOperation = getProjectCoordinatesOperation(pcs2);
        if (projectOperation != null) {
          operations.add(projectOperation);
        }
        final Unit<Length> linearUnit2 = pcs2.getLengthUnit();
        if (!linearUnit2.equals(Units.METRE)) {
          operations.add(new UnitConverstionOperation(Units.METRE, linearUnit2));
        }
      } else if (cs2 instanceof GeographicCoordinateSystem) {
        final GeographicCoordinateSystem gcs2 = (GeographicCoordinateSystem)cs2;
        final Unit<Angle> angularUnit2 = gcs2.getUnit();
        if (!angularUnit2.equals(Units.RADIAN)) {
          operations.add(new UnitConverstionOperation(Units.RADIAN, angularUnit2, 2));
        }
      }
      switch (operations.size()) {
        case 0:
          return null;
        case 1:
          return operations.get(0);
        default:
          return new ChainedCoordinatesOperation(operations);
      }
    }
  }

  public static CoordinatesOperation getCoordinatesOperation(
    final GeometryFactory sourceGeometryFactory, final GeometryFactory targetGeometryFactory) {
    final CoordinateSystem sourceCoordinateSystem = sourceGeometryFactory.getCoordinateSystem();
    final CoordinateSystem targetCoordinateSystem = targetGeometryFactory.getCoordinateSystem();
    return getCoordinatesOperation(sourceCoordinateSystem, targetCoordinateSystem);
  }

  public static CoordinatesProjection getCoordinatesProjection(
    final CoordinateSystem coordinateSystem) {
    return coordinateSystem.getCoordinatesProjection();
  }

  /**
   * Get the operation to convert coordinates to geographics coordinates.
   *
   * @param coordinateSystem The coordinate system.
   * @return The coordinates operation.
   */
  public static CoordinatesOperation getInverseCoordinatesOperation(
    final CoordinateSystem coordinateSystem) {
    final CoordinatesProjection projection = getCoordinatesProjection(coordinateSystem);
    if (projection == null) {
      return null;
    } else {
      return projection.getInverseOperation();
    }
  }

  /**
   * Get the operation to convert geographics coordinates to projected
   * coordinates.
   *
   * @param coordinateSystem The coordinate system.
   * @return The coordinates operation.
   */
  public static CoordinatesOperation getProjectCoordinatesOperation(
    final CoordinateSystem coordinateSystem) {
    final CoordinatesProjection projection = getCoordinatesProjection(coordinateSystem);
    if (projection == null) {
      return new CopyOperation();
    } else {
      return projection.getProjectOperation();
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
