package com.revolsys.io;

import java.util.Set;

import com.revolsys.gis.cs.CoordinateSystem;

public interface IoFactoryWithCoordinateSystem {
  Set<CoordinateSystem> getCoordinateSystems();

  default boolean isCoordinateSystemSupported(final CoordinateSystem coordinateSystem) {
    final Set<CoordinateSystem> coordinateSystems = getCoordinateSystems();
    return coordinateSystems.contains(coordinateSystem);
  }
}
