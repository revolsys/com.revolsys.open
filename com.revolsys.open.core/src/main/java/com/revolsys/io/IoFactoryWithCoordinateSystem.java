package com.revolsys.io;

import java.util.Set;

import org.jeometry.coordinatesystem.model.CoordinateSystem;

public interface IoFactoryWithCoordinateSystem extends IoFactory {
  Set<CoordinateSystem> getCoordinateSystems();

  default boolean isCoordinateSystemSupported(final CoordinateSystem coordinateSystem) {
    final Set<CoordinateSystem> coordinateSystems = getCoordinateSystems();
    return coordinateSystems.contains(coordinateSystem);
  }
}
