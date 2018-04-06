package com.revolsys.geometry.cs.projection;

public class ProjectOperation implements CoordinatesOperation {
  private final CoordinatesProjection projection;

  public ProjectOperation(final CoordinatesProjection projection) {
    this.projection = projection;
  }

  @Override
  public void perform(final CoordinatesOperationPoint point) {
    point.x = Math.toRadians(point.x);
    point.y = Math.toRadians(point.y);
    this.projection.project(point);
  }

  @Override
  public String toString() {
    return "geographics -> " + this.projection;
  }
}
