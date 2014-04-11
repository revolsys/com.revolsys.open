package com.revolsys.gis.cs.projection;

import com.revolsys.jts.geom.Coordinates;

public class ProjectOperation implements CoordinatesOperation {
  private final CoordinatesProjection projection;

  public ProjectOperation(final CoordinatesProjection projection) {
    this.projection = projection;
  }

  @Override
  public void perform(final Coordinates from, final Coordinates to) {
    projection.project(from, to);
  }
}
