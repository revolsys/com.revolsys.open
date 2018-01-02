package com.revolsys.geometry.cs.epsg;

import com.revolsys.geometry.cs.VerticalCoordinateSystem;

public class EpsgVerticalCoordinateSystem implements VerticalCoordinateSystem {

  private final int coordinateSystemId;

  private final String coordinateSystemName;

  private final String datumName;

  public EpsgVerticalCoordinateSystem(final int coordinateSystemId,
    final String coordinateSystemName, final String datumName) {
    super();
    this.coordinateSystemId = coordinateSystemId;
    this.coordinateSystemName = coordinateSystemName;
    this.datumName = datumName;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof EpsgVerticalCoordinateSystem) {
      final EpsgVerticalCoordinateSystem coordinateSystem = (EpsgVerticalCoordinateSystem)obj;
      return coordinateSystem.coordinateSystemId == coordinateSystem.getCoordinateSystemId();
    }
    return false;
  }

  @Override
  public int getCoordinateSystemId() {
    return this.coordinateSystemId;
  }

  @Override
  public String getCoordinateSystemName() {
    return this.coordinateSystemName;
  }

  @Override
  public String getDatumName() {
    return this.datumName;
  }

  @Override
  public int hashCode() {
    return Integer.hashCode(this.coordinateSystemId);
  }

  @Override
  public String toString() {
    return this.coordinateSystemName;
  }
}
