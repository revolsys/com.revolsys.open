package com.revolsys.gis.cs.projection;

import com.revolsys.gis.model.coordinates.Coordinates;

public class ProjectOperation implements CoordinatesOperation {
  private final CoordinatesProjection projection;

  public ProjectOperation(final CoordinatesProjection projection) {
    this.projection = projection;
  }

  public void perform(final Coordinates from, final Coordinates to) {
    projection.project(from, to);
  }
}
