package com.revolsys.io;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;

public abstract class AbstractIoFactoryWithCoordinateSystem extends AbstractIoFactory
  implements IoFactoryWithCoordinateSystem {
  private Set<CoordinateSystem> coordinateSystems = EpsgCoordinateSystems.getCoordinateSystems();

  public AbstractIoFactoryWithCoordinateSystem(final String name) {
    super(name);
  }

  @Override
  public Set<CoordinateSystem> getCoordinateSystems() {
    return this.coordinateSystems;
  }

  protected void setCoordinateSystems(final CoordinateSystem... coordinateSystems) {
    setCoordinateSystems(new LinkedHashSet<>(Arrays.asList(coordinateSystems)));
  }

  protected void setCoordinateSystems(final Set<CoordinateSystem> coordinateSystems) {
    this.coordinateSystems = coordinateSystems;
  }
}
